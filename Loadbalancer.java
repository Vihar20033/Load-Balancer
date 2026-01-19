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

// -------------------- Request Class --------------------Representing a client request
class Request {
public:
    string id;
    string requestType;
    unordered_map<string, string> parameters;

    Request(string reqId = "", string type = "")
        : id(reqId), requestType(type) {}
};

// -------------------- Destination Class --------------------Representing a server destination
class Destination {
public:
    string ipAddress;
    int requestsBeingServed;
    int threshold;  // Max concurrent requests allowed

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
        return false; // Loadbalancer serve request to other destination
    }

    void completeRequest() {
        if (requestsBeingServed > 0) {
            requestsBeingServed--;
            cout << "✔️ Request completed by " << ipAddress
                 << " | Currently serving: " << requestsBeingServed << endl;
        }
    }
};

// -------------------- Service Class --------------------Representing a group of server destinations
class Service {
public:
    string name;
    set<Destination*> destinations;

    // Add a destination to the service
    void addDestination(Destination* destination) {
        destinations.insert(destination);
    }

    // Remove a destination from the service
    void removeDestination(Destination* destination) {
        destinations.erase(destination);
    }
};

// -------------------- Abstract LoadBalancer --------------------Representing the load balancer interface decide which algorithm to use
class LoadBalancer {

protected:
    unordered_map<string, Service*> serviceMap;

public:
    // Destructor to clean up resources if needed when derived classes are deleted
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
import java.util.*;

// -------------------- Request Class --------------------
class Request {
    String id;
    String requestType;
    Map<String, String> parameters;

    Request(String reqId, String type) {
        this.id = reqId;
        this.requestType = type;
        this.parameters = new HashMap<>();
    }
}

// -------------------- Destination Class --------------------
class Destination {
    String ipAddress;
    int requestsBeingServed;
    int threshold;  // Max concurrent requests allowed

    Destination(String ip, int thresh) {
        this.ipAddress = ip;
        this.threshold = thresh;
        this.requestsBeingServed = 0;
    }

    boolean acceptRequest() {
        if (requestsBeingServed < threshold) {
            requestsBeingServed++;
            System.out.println("✅ Request accepted by " + ipAddress +
                    " | Currently serving: " + requestsBeingServed);
            return true;
        }
        System.out.println("❌ Request rejected by " + ipAddress + " (Overloaded)");
        return false;
    }

    void completeRequest() {
        if (requestsBeingServed > 0) {
            requestsBeingServed--;
            System.out.println("✔️ Request completed by " + ipAddress +
                    " | Currently serving: " + requestsBeingServed);
        }
    }
}

// -------------------- Service Class --------------------
class Service {
    String name;
    Set<Destination> destinations = new HashSet<>();

    void addDestination(Destination destination) {
        destinations.add(destination);
    }

    void removeDestination(Destination destination) {
        destinations.remove(destination);
    }
}

// -------------------- Abstract LoadBalancer --------------------
abstract class LoadBalancer {
    protected Map<String, Service> serviceMap = new HashMap<>();

    void registerService(String type, Service service) {
        serviceMap.put(type, service);
    }

    Set<Destination> getDestinations(Request request) {
        if (!serviceMap.containsKey(request.requestType)) {
            throw new RuntimeException("No service found for request type: " + request.requestType);
        }
        return serviceMap.get(request.requestType).destinations;
    }

    abstract Destination balanceLoad(Request request);
}

// -------------------- Least Connection Algorithm --------------------
class LeastConnectionLoadBalancer extends LoadBalancer {
    @Override
    Destination balanceLoad(Request request) {
        Set<Destination> destinations = getDestinations(request);

        if (destinations.isEmpty()) throw new RuntimeException("No destinations available.");

        Destination minDest = null;
        for (Destination d : destinations) {
            if (minDest == null || d.requestsBeingServed < minDest.requestsBeingServed) {
                minDest = d;
            }
        }
        return minDest;
    }
}

// -------------------- Routed Load Balancer --------------------
class RoutedLoadBalancer extends LoadBalancer {
    @Override
    Destination balanceLoad(Request request) {
        Set<Destination> destinations = getDestinations(request);
        if (destinations.isEmpty()) throw new RuntimeException("No destinations available.");

        List<Destination> destList = new ArrayList<>(destinations);
        int index = Math.abs(request.id.hashCode()) % destList.size();
        return destList.get(index);
    }
}

// -------------------- Round Robin Load Balancer --------------------
class RoundRobinLoadBalancer extends LoadBalancer {
    private Map<String, Queue<Destination>> destinationQueues = new HashMap<>();

    @Override
    Destination balanceLoad(Request request) {
        Set<Destination> destinations = getDestinations(request);
        if (destinations.isEmpty()) throw new RuntimeException("No destinations available.");

        Queue<Destination> q = destinationQueues.get(request.requestType);

        if (q == null) {
            q = new LinkedList<>();
            destinationQueues.put(request.requestType, q);
        }

        if (q.isEmpty()) {
            for (Destination d : destinations) {
                q.add(d);
            }
        }

        Destination destination = q.poll();
        q.add(destination);
        return destination;
    }
}

// -------------------- Main Class --------------------
public class Main {
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        // Setup Service and Destinations
        Service httpService = new Service();

        Destination d1 = new Destination("192.168.0.1", 12);
        Destination d2 = new Destination("192.168.0.2", 20);
        Destination d3 = new Destination("192.168.0.3", 15);

        httpService.addDestination(d1);
        httpService.addDestination(d2);
        httpService.addDestination(d3);

        // Load Balancers
        LoadBalancer leastConnLB = new LeastConnectionLoadBalancer();
        LoadBalancer routedLB = new RoutedLoadBalancer();
        LoadBalancer roundRobinLB = new RoundRobinLoadBalancer();

        leastConnLB.registerService("http", httpService);
        routedLB.registerService("http", httpService);
        roundRobinLB.registerService("http", httpService);

        while (true) {
            System.out.println("\nSelect Load Balancing Algorithm:");
            System.out.println("1. Least Connection");
            System.out.println("2. Routed");
            System.out.println("3. Round Robin");
            System.out.println("4. Exit");
            System.out.print("Enter choice: ");

            int choice = sc.nextInt();

            if (choice == 4) {
                System.out.println("Exiting Load Balancer...");
                break;
            }

            LoadBalancer lb = null;

            switch (choice) {
                case 1 -> lb = leastConnLB;
                case 2 -> lb = routedLB;
                case 3 -> lb = roundRobinLB;
                default -> {
                    System.out.println("Invalid choice. Try again.");
                    continue;
                }
            }

            System.out.print("Enter Request ID (numeric or string): ");
            String reqId = sc.next();

            Request req = new Request("REQ" + reqId, "http");

            try {
                Destination dest = lb.balanceLoad(req);
                System.out.println("➡️  Request routed to: " + dest.ipAddress);

                if (dest.acceptRequest()) {
                    dest.completeRequest();
                }

            } catch (Exception e) {
                System.out.println("⚠️ Error: " + e.getMessage());
            }
        }

        sc.close();
    }
}
