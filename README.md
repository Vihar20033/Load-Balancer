# ⚖️ Load Balancer in C++
## 📘 Overview

This project is a simple Load Balancer simulation implemented in C++.
It demonstrates how requests can be distributed across multiple servers to achieve efficient resource utilization, fault tolerance, and better performance.

A load balancer acts as a traffic controller between clients and backend servers.
It decides which server should handle an incoming request, ensuring that no single server is overloaded.

## 🧠 Key Concepts

* A Load Balancer distributes incoming network traffic across multiple servers to:

* Prevent overloading a single server.

* Increase reliability and availability.

* Improve response time and throughput.

* This C++ simulation implements the Round Robin Algorithm, one of the simplest load-balancing techniques.

## ⚙️ Features

* Implements a Round Robin Load Balancing algorithm.

* Simulates request handling by multiple servers.

* Supports dynamic addition/removal of servers.

* Displays which server handles which request.

* Lightweight and easy to modify.

## 🧩 Algorithm Used — Round Robin

The Round Robin algorithm assigns requests to servers in a cyclic order:

    Request 1 → Server 1

    Request 2 → Server 2

    Request 3 → Server 3

    Request 4 → Server 1 (cycle repeats)

This ensures fair distribution without tracking server load.

## 📂 Project Structure
    📁 LoadBalancer

    ┣ 📄 Main.cpp

    ┗ 📄 README.md

## 💻 Example Output
    Server 1 handling request 1
    Server 2 handling request 2
    Server 3 handling request 3
    Server 1 handling request 4
    Server 2 handling request 5
    Server 3 handling request 6


## ✅ Advantages

🔁 Evenly distributes traffic among servers.

💪 Prevents single-point failures.

⚡ Improves response time and system performance.

📈 Scalable — easily add/remove servers.

🧩 Simple and easy to implement.

## ⚠️ Disadvantages

❌ Doesn’t consider server load or health.

❌ Not ideal when servers have unequal capacities.

❌ No failover detection mechanism in basic implementation.

## 🚀 How to Run

Open the folder in VS Code or terminal.

* Compile the code using:

        g++ Main.cpp -o loadbalancer


* Run the program:

        ./loadbalancer


* For Windows:

        loadbalancer.exe

## 🧰 Future Improvements

* Add Dynamic Load Monitoring (based on CPU or memory).

* Implement Least Connection or Weighted Round Robin algorithms.

* Add Health Check Mechanism for server status.

* Build a simple GUI or Web Interface for visualization.

## 🏁 Conclusion

This project provides a foundational understanding of how Load Balancers work.
It can be extended into more advanced versions, integrating with real servers, network sockets, or cloud-based systems.

✨ Author

    Vihar Chudasama
    🎓 B.E. Computer Science | 
    💻 C++, Node.js, 
    Full-Stack Developer
    📅 2025