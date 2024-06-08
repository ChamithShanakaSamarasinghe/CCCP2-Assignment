import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

// Singleton Stock Class
class Stock {
    private static volatile Stock instance;
    private List<Item> items;

    private Stock() {
        this.items = Collections.synchronizedList(new ArrayList<Item>());
    }

    public static Stock getInstance() {
        if (instance == null) {
            synchronized (Stock.class) {
                if (instance == null) {
                    instance = new Stock();
                }
            }
        }
        return instance;
    }

    public synchronized void addItem(Item item) {
        items.add(item);
    }

    public synchronized void reduceItem(String code, int quantity) {
        for (Item item : items) {
            if (item.getCode().equals(code)) {
                if (item.getQuantity() >= quantity) {
                    item.setQuantity(item.getQuantity() - quantity);
                } else {
                    throw new IllegalArgumentException("Not enough stock");
                }
                break;
            }
        }
    }

    public synchronized List<Item> getReorderLevels() {
        List<Item> reorderItems = new ArrayList<>();
        for (Item item : items) {
            if (item.getQuantity() < 50) {
                reorderItems.add(item);
            }
        }
        return reorderItems;
    }

    public synchronized List<Item> getCurrentStock() {
        return new ArrayList<>(items);
    }
}

// Item Factory Class
class ItemFactory {
    public static Item createItem(String code, String name, double price, int quantity, LocalDate expiryDate) {
        return new Item(code, name, price, quantity, expiryDate);
    }
}

// Item Class
class Item {
    private String code;
    private String name;
    private double price;
    private int quantity;
    private LocalDate expiryDate;

    public Item(String code, String name, double price, int quantity, LocalDate expiryDate) {
        this.code = code;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.expiryDate = expiryDate;
    }

    // Getters and Setters
    public String getCode() { return code; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public LocalDate getExpiryDate() { return expiryDate; }

    public void setQuantity(int quantity) { this.quantity = quantity; }
}

// Bill Class
class Bill {
    private int serialNumber;
    private LocalDate date;
    private List<Item> items;
    private double totalPrice;
    private double discount;
    private double cashTendered;
    private double change;

    public Bill(int serialNumber, LocalDate date, List<Item> items, double totalPrice, double discount, double cashTendered, double change) {
        this.serialNumber = serialNumber;
        this.date = date;
        this.items = items;
        this.totalPrice = totalPrice;
        this.discount = discount;
        this.cashTendered = cashTendered;
        this.change = change;
    }

    // Getters and Setters
    public int getSerialNumber() { return serialNumber; }
    public LocalDate getDate() { return date; }
    public List<Item> getItems() { return items; }
    public double getTotalPrice() { return totalPrice; }
    public double getDiscount() { return discount; }
    public double getCashTendered() { return cashTendered; }
    public double getChange() { return change; }
}

// BillingSystem Class
class BillingSystem {
    private Stock stock;
    private List<Bill> bills;
    private static int billCounter = 1;

    public BillingSystem() {
        this.stock = Stock.getInstance();
        this.bills = Collections.synchronizedList(new ArrayList<Bill>());
    }

    public Bill processTransaction(List<Item> items, double cashTendered) {
        double totalDue = 0;
        for (Item item : items) {
            totalDue += item.getPrice() * item.getQuantity();
        }
        double change = cashTendered - totalDue;

        synchronized (stock) {
            for (Item item : items) {
                stock.reduceItem(item.getCode(), item.getQuantity());
            }
        }

        Bill bill = new Bill(billCounter++, LocalDate.now(), items, totalDue, 0, cashTendered, change);
        synchronized (bills) {
            bills.add(bill);
        }
        return bill;
    }

    public List<Bill> getBills() {
        synchronized (bills) {
            return new ArrayList<>(bills);
        }
    }

    public Stock getStock() {
        return stock;
    }
}

// Transaction Class implementing Runnable
class Transaction implements Runnable {
    private BillingSystem billingSystem;
    private List<Item> items;
    private double cashTendered;

    public Transaction(BillingSystem billingSystem, List<Item> items, double cashTendered) {
        this.billingSystem = billingSystem;
        this.items = items;
        this.cashTendered = cashTendered;
    }

    @Override
    public void run() {
        Bill bill = billingSystem.processTransaction(items, cashTendered);
        System.out.println("Transaction completed: " + bill.getSerialNumber() + ", Change: " + bill.getChange());
    }
}

// ReportGenerator Class
class ReportGenerator {
    public void generateTotalSalesReport(List<Bill> bills) {
        System.out.println("Total Sales Report");
        double totalRevenue = 0;
        for (Bill bill : bills) {
            totalRevenue += bill.getTotalPrice();
            for (Item item : bill.getItems()) {
                System.out.println("Item: " + item.getName() + ", Code: " + item.getCode() + ", Quantity: " + item.getQuantity());
            }
        }
        System.out.println("Total Revenue: " + totalRevenue);
    }

    public void generateReshelvedItemsReport(List<Item> items) {
        System.out.println("Reshelved Items Report");
        for (Item item : items) {
            System.out.println("Item: " + item.getName() + ", Code: " + item.getCode() + ", Quantity: " + item.getQuantity());
        }
    }

    public void generateReorderLevelsReport(List<Item> items) {
        System.out.println("Reorder Levels Report");
        for (Item item : items) {
            if (item.getQuantity() < 50) {
                System.out.println("Item: " + item.getName() + ", Code: " + item.getCode() + ", Quantity: " + item.getQuantity());
            }
        }
    }

    public void generateStockReport(List<Item> items) {
        System.out.println("Stock Report");
        for (Item item : items) {
            System.out.println("Item: " + item.getName() + ", Code: " + item.getCode() + ", Quantity: " + item.getQuantity() + ", Expiry Date: " + item.getExpiryDate());
        }
    }

    public void generateBillReport(List<Bill> bills) {
        System.out.println("Bill Report");
        for (Bill bill : bills) {
            System.out.println("Bill Serial Number: " + bill.getSerialNumber() + ", Date: " + bill.getDate() + ", Total Price: " + bill.getTotalPrice() + ", Cash Tendered: " + bill.getCashTendered() + ", Change: " + bill.getChange());
        }
    }
}

// Main Class to demonstrate the functionality
public class SYOSBillingSystem {
    public static void main(String[] args) throws InterruptedException {
        BillingSystem billingSystem = new BillingSystem();

        // Adding initial stock
        Stock stock = billingSystem.getStock();
        stock.addItem(ItemFactory.createItem("ITEM001", "Milk", 100, 200, LocalDate.of(2024, 12, 1)));
        stock.addItem(ItemFactory.createItem("ITEM002", "Bread", 50, 150, LocalDate.of(2024, 6, 1)));

        // Creating an ExecutorService with a fixed thread pool
        ExecutorService executor = Executors.newFixedThreadPool(5);

        // Simulating multiple transactions
        List<Item> items1 = new ArrayList<>();
        items1.add(new Item("ITEM001", "Milk", 100, 2, LocalDate.of(2024, 12, 1)));
        items1.add(new Item("ITEM002", "Bread", 50, 3, LocalDate.of(2024, 6, 1)));

        List<Item> items2 = new ArrayList<>();
        items2.add(new Item("ITEM001", "Milk", 100, 1, LocalDate.of(2024, 12, 1)));
        items2.add(new Item("ITEM002", "Bread", 50, 1, LocalDate.of(2024, 6, 1)));

        executor.submit(new Transaction(billingSystem, items1, 400));
        executor.submit(new Transaction(billingSystem, items2, 200));

        // Shutting down the executor service
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.MINUTES);

        // Generating reports
        ReportGenerator reportGenerator = new ReportGenerator();
        reportGenerator.generateTotalSalesReport(billingSystem.getBills());
        reportGenerator.generateStockReport(stock.getCurrentStock());
        reportGenerator.generateReorderLevelsReport(stock.getReorderLevels());
    }
}
