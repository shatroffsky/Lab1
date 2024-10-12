import java.util.concurrent.Semaphore; // Імпорт класу Semaphore для синхронізації
import java.time.LocalTime; // Імпорт класу LocalTime для роботи з часом

// Клас для системи бронювання квитків
class TicketBookingSystem {
    private static final int TOTAL_TICKETS = 10; // Загальна кількість квитків
    private int availableTickets = TOTAL_TICKETS; // Доступні квитки
    private Semaphore semaphore = new Semaphore(1); // Семафор для управління доступом до квитків

    // Метод бронювання квитків
    public void bookTicket(String customerName) {
        try {
            // Отримуємо поточний час
            LocalTime now = LocalTime.now();
            // Перевірка, чи поточний час в межах заборонених для бронювання
            if (now.isBefore(LocalTime.of(6, 0)) && now.isAfter(LocalTime.of(0, 0))) {
                // Вивід повідомлення про неможливість бронювання
                System.out.println(customerName + ": Бронювання неможливе з 00:00 до 06:00.");
                return; // Вихід з методу, якщо час не дозволяє бронювання
            }

            // Отримуємо доступ до критичної секції
            semaphore.acquire();

            // Перевіряємо наявність квитків
            if (availableTickets > 0) {
                availableTickets--; // Зменшуємо кількість доступних квитків
                // Вивід повідомлення про успішне бронювання
                System.out.println(customerName + " забронював(ла) квиток. Залишилось квитків: " + availableTickets);
            } else {
                // Вивід повідомлення, якщо квитків немає
                System.out.println(customerName + ": Немає доступних квитків.");
            }
        } catch (InterruptedException e) {
            // Обробка можливих помилок під час бронювання
            System.out.println(customerName + ": Сталася помилка під час бронювання.");
        } finally {
            // Завжди звільняємо семафор, навіть у разі помилки
            semaphore.release();
        }
    }

    // Метод для перевірки кількості доступних квитків
    public int getAvailableTickets() {
        return availableTickets; // Повертаємо кількість доступних квитків
    }
}

// Клас для клієнтів, які намагаються забронювати квитки
class Customer implements Runnable {
    private TicketBookingSystem bookingSystem; // Посилання на систему бронювання
    private String name; // Ім'я клієнта

    // Конструктор класу Customer
    public Customer(TicketBookingSystem bookingSystem, String name) {
        this.bookingSystem = bookingSystem; // Ініціалізація системи бронювання
        this.name = name; // Ініціалізація імені клієнта
    }

    @Override
    public void run() {
        // Виклик методу бронювання для даного клієнта
        bookingSystem.bookTicket(name);
    }
}

// Головний клас програми
public class Main {
    public static void main(String[] args) {
        // Створення екземпляра системи бронювання
        TicketBookingSystem system = new TicketBookingSystem();

        // Створення масиву потоків для клієнтів
        Thread[] customers = new Thread[12]; // 12 клієнтів намагаються забронювати 10 квитків

        // Ініціалізація і запуск потоків клієнтів
        for (int i = 0; i < customers.length; i++) {
            customers[i] = new Thread(new Customer(system, "Клієнт " + (i + 1))); // Створення потоку для кожного клієнта
            customers[i].start(); // Запуск потоку
        }

        // Очікуємо завершення всіх потоків
        for (int i = 0; i < customers.length; i++) {
            try {
                customers[i].join(); // Очікуємо, поки потік завершиться
            } catch (InterruptedException e) {
                // Обробка помилки, якщо потік був перерваний
                e.printStackTrace();
            }
        }

        // Вивід підсумкової інформації
        System.out.println("Усі клієнти спробували забронювати квитки.");
        System.out.println("Залишилося квитків: " + system.getAvailableTickets());
    }
}
