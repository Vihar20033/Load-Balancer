#include <iostream>
#include <string>
#include <unordered_map>
#include <vector>
#include <set>
#include <queue>
#include <algorithm>
#include <stdexcept>
#include <functional>

using namespace std;

// -------------------- Request Class --------------------
class Request {
public:
    string id;
    string requestType;
    unordered_map<string, string> parameters;

    Request(string reqId = "", string type = "")
        : id(reqId), requestType(type) {}
};

// -------------------- Destination Class --------------------
class Destination {
public:
    string ipAddress;
    int requestsBeingServed;
    int threshold;

    Destination(string ip, int thresh)
        : ipAddress(ip), requestsBeingServed(0), threshold(thresh) {}

    bool acceptRequest() {
        if (requestsBeingServed < threshold) {
            requestsBeingServed++;
            cout << "✅ Request accepted by " << ipAddress
                 << " | Currently serving: " << requestsBeingServed << endl;
            return true;
        }
        cout << "❌ Request rejected by " << ipAddress << " (Overloaded)\n";
        return false;
    }

    void completeRequest() {
        if (requestsBeingServed > 0) {
            requestsBeingServed--;
            cout << "✔️ Request completed by " << ipAddress
                 << " | Currently serving: " << requestsBeingServed << endl;
        }
    }
};

// -------------------- Service Class --------------------
class Service {
public:
    string name;
    set<Destination*> destinations;

    void addDestination(Destination* destination) {
        destinations.insert(destination);
    }

    void removeDestination(Destination* destination) {
        destinations.erase(destination);
    }
};

// -------------------- Abstract LoadBalancer --------------------
class LoadBalancer {
protected:
    unordered_map<string, Service*> serviceMap;

public:
    virtual ~LoadBalancer() = default;

    void registerService(const string& type, Service* service) {
        serviceMap[type] = service;
    }

    set<Destination*>& getDestinations(Request& request) {
        if (serviceMap.find(request.requestType) == serviceMap.end()) {
            throw runtime_error("No service found for request type: " + request.requestType);
        }
        return serviceMap[request.requestType]->destinations;
    }

    virtual Destination* balanceLoad(Request& request) = 0;
};

// -------------------- Least Connection Algorithm --------------------
class LeastConnectionLoadBalancer : public LoadBalancer {
public:
    Destination* balanceLoad(Request& request) override {
        auto& destinations = getDestinations(request);
        if (destinations.empty()) throw runtime_error("No destinations available.");

        return *min_element(destinations.begin(), destinations.end(),
                            [](Destination* a, Destination* b) {
                                return a->requestsBeingServed < b->requestsBeingServed;
                            });
    }
};

// -------------------- Routed Load Balancer --------------------
class RoutedLoadBalancer : public LoadBalancer {
public:
    Destination* balanceLoad(Request& request) override {
        auto& destinations = getDestinations(request);
        if (destinations.empty()) throw runtime_error("No destinations available.");

        vector<Destination*> destList(destinations.begin(), destinations.end());
        size_t index = hash<string>{}(request.id) % destList.size();
        return destList[index];
    }
};

// -------------------- Round Robin Load Balancer --------------------
class RoundRobinLoadBalancer : public LoadBalancer {
private:
    unordered_map<string, queue<Destination*>> destinationQueues;

public:
    Destination* balanceLoad(Request& request) override {
        auto& destinations = getDestinations(request);
        if (destinations.empty()) throw runtime_error("No destinations available.");

        auto& q = destinationQueues[request.requestType];
        if (q.empty()) {
            for (auto* dest : destinations) q.push(dest);
        }

        Destination* destination = q.front();
        q.pop();
        q.push(destination);
        return destination;
    }
};

// -------------------- Main Function --------------------
int main() {
    // Setup Service and Destinations
    Service httpService;
    Destination d1("192.168.0.1", 12);
    Destination d2("192.168.0.2", 20);
    Destination d3("192.168.0.3", 15);

    httpService.addDestination(&d1);
    httpService.addDestination(&d2);
    httpService.addDestination(&d3);

    // Load Balancers
    LeastConnectionLoadBalancer leastConnLB;
    RoutedLoadBalancer routedLB;
    RoundRobinLoadBalancer roundRobinLB;

    leastConnLB.registerService("http", &httpService);
    routedLB.registerService("http", &httpService);
    roundRobinLB.registerService("http", &httpService);

    while (true) {
        cout << "\nSelect Load Balancing Algorithm:\n";
        cout << "1. Least Connection\n";
        cout << "2. Routed\n";
        cout << "3. Round Robin\n";
        cout << "4. Exit\n";
        cout << "Enter choice: ";

        int choice;
        cin >> choice;

        if (choice == 4) {
            cout << "Exiting Load Balancer...\n";
            break;
        }

        LoadBalancer* lb = nullptr;
        switch (choice) {
            case 1: lb = &leastConnLB; break;
            case 2: lb = &routedLB; break;
            case 3: lb = &roundRobinLB; break;
            default:
                cout << "Invalid choice. Try again.\n";
                continue;
        }

        string reqId;
        cout << "Enter Request ID (numeric or string): ";
        cin >> reqId;

        Request req("REQ" + reqId, "http");

        try {
            Destination* dest = lb->balanceLoad(req);
            cout << "➡️  Request routed to: " << dest->ipAddress << endl;

            if (dest->acceptRequest()) {
                dest->completeRequest();
            }
        } catch (const exception& e) {
            cerr << "⚠️  Error: " << e.what() << endl;
        }
    }

    return 0;
}
