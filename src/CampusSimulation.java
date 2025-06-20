import java.util.Scanner;

public class CampusSimulation {

    static class Event {
        public static final int PRIORITY_MANDATORY = 3;
        public static final int PRIORITY_HIGH = 2;
        public static final int PRIORITY_OPTIONAL = 1;
        private String eventId, type, location;
        private int startTime, endTime, priority;

        public Event(String eventId, String type, String location, int startTime, int endTime, int priority) {
            this.eventId = eventId;
            this.type = type;
            this.location = location;
            this.startTime = startTime;
            this.endTime = endTime;
            this.priority = priority;
        }

        public String getEventId() { return eventId; }
        public String getType() { return type; }
        public String getLocation() { return location; }
        public int getStartTime() { return startTime; }
        public int getEndTime() { return endTime; }
        public int getPriority() { return priority; }

        public String toString() {
            return String.format("Event[%s: %s at %s, %d-%d, Priority: %d]", eventId, type, location, startTime, endTime, priority);
        }
    }

    static class EventMaxHeap {
        private Event[] heap;
        private int size;
        private static final int CAPACITY = 100;

        public EventMaxHeap() {
            heap = new Event[CAPACITY];
            size = 0;
        }

        private int parent(int i) { return (i - 1) / 2; }
        private int left(int i) { return 2 * i + 1; }
        private int right(int i) { return 2 * i + 2; }

        private int compare(Event a, Event b) {
            if (a.getPriority() != b.getPriority()) return a.getPriority() - b.getPriority();
            return b.getStartTime() - a.getStartTime();
        }

        public void insert(Event e) {
            if (size == CAPACITY) return;
            heap[size] = e;
            int i = size;
            size++;
            while (i > 0 && compare(heap[i], heap[parent(i)]) > 0) {
                Event tmp = heap[i];
                heap[i] = heap[parent(i)];
                heap[parent(i)] = tmp;
                i = parent(i);
            }
        }

        public Event extractMax() {
            if (size == 0) return null;
            Event max = heap[0];
            heap[0] = heap[size - 1];
            size--;
            heapify(0);
            return max;
        }

        private void heapify(int i) {
            int largest = i, l = left(i), r = right(i);
            if (l < size && compare(heap[l], heap[largest]) > 0) largest = l;
            if (r < size && compare(heap[r], heap[largest]) > 0) largest = r;
            if (largest != i) {
                Event tmp = heap[i];
                heap[i] = heap[largest];
                heap[largest] = tmp;
                heapify(largest);
            }
        }

        public boolean isEmpty() { return size == 0; }
        public int size() { return size; }

        public Event get(int idx) {
            if (idx < 0 || idx >= size) return null;
            return heap[idx];
        }

        public Event[] toArray() {
            Event[] arr = new Event[size];
            for (int i = 0; i < size; i++) arr[i] = heap[i];
            return arr;
        }
    }

    static class RoomTracker {
        private String[] rooms = {"Mainhall", "Library", "Cafeteria", "Lab", "Hostel", "Guardroom"};
        private int[][][] schedules = new int[6][20][2];
        private int[] count = new int[6];

        private int index(String room) {
            for (int i = 0; i < rooms.length; i++) if (rooms[i].equals(room)) return i;
            return -1;
        }

        public boolean isAvailable(String room, int start, int end) {
            int idx = index(room);
            if (idx == -1) return false;
            for (int i = 0; i < count[idx]; i++) {
                int s = schedules[idx][i][0], e = schedules[idx][i][1];
                if (!(end <= s || start >= e)) return false;
            }
            return true;
        }

        public boolean book(String room, int start, int end) {
            int idx = index(room);
            if (idx == -1 || !isAvailable(room, start, end)) return false;
            schedules[idx][count[idx]][0] = start;
            schedules[idx][count[idx]][1] = end;
            count[idx]++;
            return true;
        }

        public String findAlt(int start, int end) {
            for (String room : rooms) if (isAvailable(room, start, end)) return room;
            return null;
        }

        public void show(Event[] events) {
            System.out.println("\nRoom Schedule:");
            for (int i = 0; i < rooms.length; i++) {
                System.out.print(rooms[i] + ": ");
                for (int j = 0; j < count[i]; j++) {
                    int s = schedules[i][j][0];
                    int e = schedules[i][j][1];
                    System.out.print("[" + s + "-" + e + "] ");
                    for (Event ev : events) {
                        if (ev.getLocation().equals(rooms[i]) && ev.getStartTime() == s && ev.getEndTime() == e) {
                            System.out.print("=> " + ev.toString() + " ");
                        }
                    }
                }
                System.out.println();
            }
        }

        public boolean cancel(String room, int start, int end) {
            int idx = index(room);
            if (idx == -1) return false;
            for (int i = 0; i < count[idx]; i++) {
                if (schedules[idx][i][0] == start && schedules[idx][i][1] == end) {
                    for (int j = i; j < count[idx] - 1; j++)
                        schedules[idx][j] = schedules[idx][j + 1];
                    count[idx]--;
                    return true;
                }
            }
            return false;
        }
    }

    static class Graph {
        private String[] nodes;
        private int[][] adj;
        private int n;

        public Graph(String[] locations) {
            this.nodes = locations;
            this.n = locations.length;
            adj = new int[n][n];
            for (int i = 0; i < n; i++)
                for (int j = 0; j < n; j++)
                    adj[i][j] = (i == j) ? 0 : 999999;
        }

        public int index(String name) {
            for (int i = 0; i < n; i++) if (nodes[i].equals(name)) return i;
            return -1;
        }

        public void addEdge(String from, String to, int weight) {
            int i = index(from), j = index(to);
            if (i != -1 && j != -1) {
                adj[i][j] = weight;
                adj[j][i] = weight;
            }
        }

        public void shortestPath(String from, String to) {
            int src = index(from), dest = index(to);
            if (src == -1 || dest == -1) {
                System.out.println("Invalid locations.");
                return;
            }
            int[] dist = new int[n];
            boolean[] visited = new boolean[n];
            int[] prev = new int[n];
            for (int i = 0; i < n; i++) {
                dist[i] = 999999;
                prev[i] = -1;
            }
            dist[src] = 0;

            for (int count = 0; count < n; count++) {
                int u = -1, min = 999999;
                for (int i = 0; i < n; i++)
                    if (!visited[i] && dist[i] < min) {
                        min = dist[i];
                        u = i;
                    }
                if (u == -1) break;
                visited[u] = true;
                for (int v = 0; v < n; v++) {
                    if (adj[u][v] < 999999 && !visited[v]) {
                        if (dist[v] > dist[u] + adj[u][v]) {
                            dist[v] = dist[u] + adj[u][v];
                            prev[v] = u;
                        }
                    }
                }
            }
            if (dist[dest] == 999999) {
                System.out.println("No route found.");
                return;
            }
            // Print path
            System.out.print("Shortest path: ");
            printPath(prev, dest);
            System.out.println(" (Distance: " + dist[dest] + ")");
        }

        private void printPath(int[] prev, int j) {
            if (prev[j] == -1) {
                System.out.print(nodes[j]);
                return;
            }
            printPath(prev, prev[j]);
            System.out.print(" -> " + nodes[j]);
        }
    }

    public static void main(String[] args) {
        EventMaxHeap heap = new EventMaxHeap();
        RoomTracker rooms = new RoomTracker();

        String[] locations = {"Mainhall", "Library", "Cafeteria", "Lab", "Hostel", "Guardroom"};
        Graph campus = new Graph(locations);
        campus.addEdge("Mainhall", "Library", 2);
        campus.addEdge("Mainhall", "Cafeteria", 4);
        campus.addEdge("Library", "Lab", 3);
        campus.addEdge("Lab", "Hostel", 6);
        campus.addEdge("Hostel", "Guardroom", 2);
        campus.addEdge("Cafeteria", "Guardroom", 5);
        campus.addEdge("Library", "Cafeteria", 3);

        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\n1. Add Event");
            System.out.println("2. Update Event");
            System.out.println("3. Remove Event");
            System.out.println("4. Show Schedule");
            System.out.println("5. Find Route");
            System.out.println("6. Exit");
            System.out.print("Choose option: ");
            int opt = Integer.parseInt(sc.nextLine());

            if (opt == 1) {
                System.out.print("Event ID: "); String id = sc.nextLine();
                System.out.print("Type: "); String type = sc.nextLine();
                System.out.print("Location (Mainhall/Library/Cafeteria/Lab/Hostel/Guardroom): "); String loc = sc.nextLine();
                System.out.print("Start Time: "); int start = Integer.parseInt(sc.nextLine());
                System.out.print("End Time: "); int end = Integer.parseInt(sc.nextLine());
                System.out.print("Priority (1=Optional,2=High,3=Mandatory): "); int prio = Integer.parseInt(sc.nextLine());
                if (!rooms.book(loc, start, end)) {
                    String alt = rooms.findAlt(start, end);
                    if (alt != null) {
                        System.out.println("Room " + loc + " unavailable. Using alternative: " + alt);
                        loc = alt;
                        rooms.book(loc, start, end);
                    } else {
                        System.out.println("No available rooms for event " + id);
                        continue;
                    }
                }
                heap.insert(new Event(id, type, loc, start, end, prio));
                System.out.println("Scheduled: " + id + " in " + loc);

            } else if (opt == 2) {
                System.out.print("Event ID to update: "); String id = sc.nextLine();
                Event[] all = heap.toArray();
                Event found = null;
                for (Event e : all) if (e.getEventId().equals(id)) found = e;
                if (found == null) {
                    System.out.println("Event not found.");
                    continue;
                }
                heap = new EventMaxHeap();
                for (Event e : all) if (!e.getEventId().equals(id)) heap.insert(e);
                rooms.cancel(found.getLocation(), found.getStartTime(), found.getEndTime());

                System.out.print("New Type: "); String type = sc.nextLine();
                System.out.print("New Location (Mainhall/Library/Cafeteria/Lab/Hostel/Guardroom): "); String loc = sc.nextLine();
                System.out.print("New Start Time: "); int start = Integer.parseInt(sc.nextLine());
                System.out.print("New End Time: "); int end = Integer.parseInt(sc.nextLine());
                System.out.print("New Priority (1=Optional,2=High,3=Mandatory): "); int prio = Integer.parseInt(sc.nextLine());
                if (!rooms.book(loc, start, end)) {
                    System.out.println("Room unavailable. Update failed.");
                    continue;
                }
                heap.insert(new Event(id, type, loc, start, end, prio));
                System.out.println("Updated: " + id);

            } else if (opt == 3) {
                System.out.print("Event ID to remove: "); String id = sc.nextLine();
                Event[] all = heap.toArray();
                boolean removed = false;
                heap = new EventMaxHeap();
                for (Event e : all) {
                    if (e.getEventId().equals(id)) {
                        rooms.cancel(e.getLocation(), e.getStartTime(), e.getEndTime());
                        removed = true;
                    } else heap.insert(e);
                }
                if (removed) System.out.println("Removed: " + id);
                else System.out.println("Event not found.");

            } else if (opt == 4) {
                rooms.show(heap.toArray());

            } else if (opt == 5) {
                System.out.print("From location (Mainhall/Library/Cafeteria/Lab/Hostel/Guardroom): ");
                String from = sc.nextLine();
                System.out.print("To location (Mainhall/Library/Cafeteria/Lab/Hostel/Guardroom): ");
                String to = sc.nextLine();
                campus.shortestPath(from, to);

            } else if (opt == 6) {
                System.out.println("Exiting simulation...");
                break;
            }
        }
        sc.close();
    }
}
