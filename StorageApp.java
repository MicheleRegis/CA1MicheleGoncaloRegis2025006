package StorageApp;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * Food Storage Application
 *
 * This single-file solution features an OOP solution for the Problem.

 * It consists of:
 * - FoodItem class (name, weight, best-before, placed time)
 * - Storage interface (operations to be executed)
 * - StackStorage (LIFO) and QueueStorage (FIFO circular queue) implementations
 * - StorageFactory to choose between stack or queue mode
 * - Application with main menu, validation and error handling.
 * FoodItem.java
   Storage.java
   StackStorage.java
   QueueStorage.java
   StorageFactory.java
   CAMicheleGRegis2025006.java
 */

// -------------------- FoodItem --------------------
class FoodItem {
    private final String name;
    private final int weight; // grams
    private final LocalDate bestBefore;
    private final LocalDateTime placedTime;

    public FoodItem(String name, int weight, LocalDate bestBefore) {
        this.name = name;
        this.weight = weight;
        this.bestBefore = bestBefore;
        this.placedTime = LocalDateTime.now();
    }

    public String getName() { return name; }
    public int getWeight() { return weight; }
    public LocalDate getBestBefore() { return bestBefore; }
    public LocalDateTime getPlacedTime() { return placedTime; }

    @Override
    public String toString() {
        return String.format("%s (%dg) - BestBefore: %s - Placed: %s",
                name, weight, bestBefore.toString(), placedTime.toString());
    }
}

// -------------------- Storage Interface --------------------
interface Storage {
    boolean add(FoodItem item);           // add food item, return false if full / invalid
    FoodItem remove();                    // remove according to structure rule, return null if empty
    FoodItem peek();                      // show top/front element without removing
    void display();                       // display all elements in storage
    int search(String name);              // return position or -1 if not found
    boolean isFull();
    boolean isEmpty();
    String topName();                      // name at top/front
    int size();                            // current number
}

// -------------------- StackStorage (LIFO) --------------------
class StackStorage implements Storage {
    private final FoodItem[] data;
    private final int capacity;
    private int top; // index of top element, -1 when empty

    public StackStorage(int capacity) {
        this.capacity = capacity;
        this.data = new FoodItem[capacity];
        this.top = -1;
    }

    @Override
    public boolean add(FoodItem item) {
        if (isFull()) return false;
        data[++top] = item;
        return true;
    }

    @Override
    public FoodItem remove() {
        if (isEmpty()) return null;
        FoodItem removed = data[top];
        data[top--] = null; // help GC
        return removed;
    }

    @Override
    public FoodItem peek() {
        if (isEmpty()) return null;
        return data[top];
    }

    @Override
    public void display() {
        if (isEmpty()) {
            System.out.println("Storage is empty.");
            return;
        }
        System.out.println("Storage (LIFO) top -> bottom:");
        for (int i = top; i >= 0; i--) {
            System.out.println("  [" + i + "] " + data[i]);
        }
    }

    @Override
    public int search(String name) {
        // linear search from top to bottom, return 0-based distance from top (0 = top)
        for (int i = top; i >= 0; i--) {
            if (data[i] != null && data[i].getName().equalsIgnoreCase(name)) {
                return top - i; // distance from top
            }
        }
        return -1;
    }

    @Override
    public boolean isFull() { return top + 1 == capacity; }

    @Override
    public boolean isEmpty() { return top == -1; }

    @Override
    public String topName() { return isEmpty() ? null : data[top].getName(); }

    @Override
    public int size() { return top + 1; }
}

// -------------------- QueueStorage (FIFO, circular) --------------------
class QueueStorage implements Storage {
    private final FoodItem[] data;
    private final int capacity;
    private int front; // index of front element
    private int rear;  // index of rear element
    private int count; // number of elements

    public QueueStorage(int capacity) {
        this.capacity = capacity;
        this.data = new FoodItem[capacity];
        this.front = 0;
        this.rear = -1;
        this.count = 0;
    }

    @Override
    public boolean add(FoodItem item) {
        if (isFull()) return false;
        rear = (rear + 1) % capacity;
        data[rear] = item;
        count++;
        return true;
    }

    @Override
    public FoodItem remove() {
        if (isEmpty()) return null;
        FoodItem removed = data[front];
        data[front] = null; // help GC
        front = (front + 1) % capacity;
        count--;
        if (count == 0) { // reset pointers to initial state (optional)
            front = 0;
            rear = -1;
        }
        return removed;
    }

    @Override
    public FoodItem peek() {
        if (isEmpty()) return null;
        return data[front];
    }

    @Override
    public void display() {
        if (isEmpty()) {
            System.out.println("Storage is empty.");
            return;
        }
        System.out.println("Storage (FIFO) front -> rear:");
        for (int i = 0, idx = front; i < count; i++, idx = (idx + 1) % capacity) {
            System.out.println("  [" + ((front + i) % capacity) + "] " + data[idx]);
        }
    }

    @Override
    public int search(String name) {
        for (int i = 0, idx = front; i < count; i++, idx = (idx + 1) % capacity) {
            if (data[idx] != null && data[idx].getName().equalsIgnoreCase(name)) {
                return i; // distance from front (0 = front)
            }
        }
        return -1;
    }

    @Override
    public boolean isFull() { return count == capacity; }

    @Override
    public boolean isEmpty() { return count == 0; }

    @Override
    public String topName() { return isEmpty() ? null : data[front].getName(); }

    @Override
    public int size() { return count; }
}

// -------------------- StorageFactory --------------------
class StorageFactory {
    public static Storage create(boolean useOppositeDoor, int capacity) {
        if (useOppositeDoor) return new QueueStorage(capacity);
        return new StackStorage(capacity);
    }
}

// -------------------- Main Application --------------------
public class StorageApp {
    private static final int CAPACITY = 8;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Choose storage mode:");
        System.out.println("1. Use only front door (LIFO - Stack)");
        System.out.println("2. Use front door to add and opposite door to remove (FIFO - Queue)");
        System.out.print("Enter choice (1 or 2): ");

        int modeChoice = readInt(scanner, 1, 2);
        boolean useOppositeDoor = (modeChoice == 2);

        Storage storage = StorageFactory.create(useOppositeDoor, CAPACITY);

        while (true) {
            System.out.println("\n--- Fast Food Storage Menu ---");
            System.out.println("1. Add food item");
            System.out.println("2. Remove food item");
            System.out.println("3. Peek top/front item");
            System.out.println("4. Display all items");
            System.out.println("5. Search item by name");
            System.out.println("6. Show storage info (size / capacity / top name)");
            System.out.println("7. Exit");
            System.out.print("Choice: ");

            int choice = readInt(scanner, 1, 7);

            switch (choice) {
                case 1 -> {
                    if (storage.isFull()) {
                        System.out.println("Cannot add: storage is full (capacity = " + CAPACITY + ")");
                        break;
                    }
                    System.out.print("Enter food name (Burger/Pizza/Fries/Sandwich/Hotdog): ");
                    String name = scanner.nextLine().trim();
                    if (!isValidFoodName(name)) {
                        System.out.println("Invalid food name. Allowed: Burger, Pizza, Fries, Sandwich, Hotdog.");
                        break;
                    }
                    System.out.print("Enter weight in grams (positive integer): ");
                    int weight = readInt(scanner, 1, Integer.MAX_VALUE);

                    System.out.print("Enter best-before date (yyyy-MM-dd) or press ENTER to use max 2 weeks from today: ");
                    String dateInput = scanner.nextLine().trim();
                    LocalDate bestBefore;
                    try {
                        if (dateInput.isEmpty()) {
                            bestBefore = LocalDate.now().plusWeeks(2);
                        } else {
                            bestBefore = LocalDate.parse(dateInput, DATE_FMT);
                        }
                    } catch (DateTimeParseException ex) {
                        System.out.println("Invalid date format. Use yyyy-MM-dd.");
                        break;
                    }
                    if (!isWithinTwoWeeks(bestBefore)) {
                        System.out.println("Best-before must be within two weeks from today and not before today.");
                        break;
                    }

                    FoodItem item = new FoodItem(name, weight, bestBefore);
                    boolean added = storage.add(item);
                    if (added) System.out.println("Added: " + item);
                    else System.out.println("Failed to add item (storage full or error).");
                }

                case 2 -> {
                    FoodItem removed = storage.remove();
                    if (removed == null) System.out.println("Storage is empty. Nothing to remove.");
                    else System.out.println("Removed: " + removed);
                }

                case 3 -> {
                    FoodItem top = storage.peek();
                    if (top == null) System.out.println("Storage is empty.");
                    else System.out.println("Top/Front item: " + top);
                }

                case 4 -> storage.display();

                case 5 -> {
                    System.out.print("Enter name to search: ");
                    String sname = scanner.nextLine().trim();
                    int pos = storage.search(sname);
                    if (pos == -1) System.out.println("Item not found.");
                    else {
                        String where = useOppositeDoor ? (pos == 0 ? "front" : pos + " from front") : (pos == 0 ? "top" : pos + " from top");
                        System.out.println("Found: distance = " + pos + " (" + where + ")");
                    }
                }

                case 6 -> {
                    System.out.println("Size: " + storage.size() + " / " + CAPACITY);
                    System.out.println("Top/Front name: " + (storage.topName() == null ? "(none)" : storage.topName()));
                    System.out.println("Is full? " + storage.isFull() + " | Is empty? " + storage.isEmpty());
                }

                case 7 -> {
                    System.out.println("Exiting. Goodbye!");
                    scanner.close();
                    return;
                }

                default -> System.out.println("Invalid choice.");
            }
        }
    }

    // -------------------- Helpers --------------------
    private static boolean isValidFoodName(String name) {
        if (name == null) return false;
        return switch (name.toLowerCase()) {
            case "burger", "pizza", "fries", "sandwich", "hotdog" -> true;
            default -> false;
        };
    }

    private static boolean isWithinTwoWeeks(LocalDate date) {
        LocalDate today = LocalDate.now();
        LocalDate max = today.plusWeeks(2);
        return (!date.isBefore(today)) && (!date.isAfter(max));
    }

    private static int readInt(Scanner scanner, int min, int max) {
        while (true) {
            String line = scanner.nextLine().trim();
            try {
                int v = Integer.parseInt(line);
                if (v < min || v > max) {
                    System.out.print("Please enter a number between " + min + " and " + max + ": ");
                    continue;
                }
                return v;
            } catch (NumberFormatException e) {
                System.out.print("Invalid number. Try again: ");
            }
        }
    }
}

