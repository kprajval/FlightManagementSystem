# ✈️ Flight Management System (Spring Boot + MySQL)

A full-stack Flight Management System built using **Spring Boot** and **MySQL**, featuring flight search, booking, seat selection, dummy payments, ticket cancellations, boarding pass generation, and real-time **SMS notifications via Twilio API**.

---

## 🚀 Features

### 🔍 1. Search Flights
- Search by source, destination, and journey date.
- Flights fetched from the `flights` table in MySQL.

### 🧾 2. Book Flights
- Add passenger details and choose flights.
- Booking saved in the `bookings` and `passengers` tables.

### 💺 3. Seat Selection
- View and select available seats in real-time.
- Seats managed in `seats` table (`is_booked` flag).

### 💳 4. Dummy Payment Gateway
- Process payment using a dummy bank account.
- Validates:
  - Account number
  - PIN
  - Sufficient balance
- Deducts balance and marks booking as **CONFIRMED**.

### ❌ 5. Cancel Ticket
- Cancel booked ticket.
- Refund dummy bank account.
- Update booking status to **CANCELLED**.

### 🎟️ 6. Boarding Pass Generation
- After successful payment:
  - Boarding pass includes passenger info, flight ID, seat number, and QR/booking ID.
  - Can be downloaded/emailed (optional).

### 📲 7. SMS Notifications using Twilio
- **Twilio API** integrated to send real-time SMS on:
  - Booking confirmation
  - Ticket cancellation
- Message sent to registered phone number from backend.

---

## 🧪 Sample Dummy Bank Account

| Account No   | PIN  | Balance  |
|--------------|------|----------|
| 1234567890   | 1234 | ₹10,000  |

Stored in `bank_accounts` table.

---

## 🗃️ MySQL Tables

1. `flights(flight_id, source, destination, date, time, price, available_seats)`
2. `passengers(passenger_id, name, age, gender, booking_id)`
3. `bookings(booking_id, flight_id, passenger_id, status, total_price, seat_no)`
4. `seats(seat_id, flight_id, seat_no, is_booked)`
5. `bank_accounts(account_no, name, balance, pin)`
6. `boarding_pass(booking_id, flight_id, passenger_id, seat_no, issue_date)`

---

## 🔧 Technologies Used

- **Backend:** Spring Boot (Java)
- **Database:** MySQL
- **SMS API:** Twilio
- **ORM:** Spring Data JPA
- **Build Tool:** Maven
- **Security:** Spring Security (if authentication added)

---


## 📲 Twilio Setup

1. **Create a Twilio account** at [https://www.twilio.com](https://www.twilio.com).
2. Get your:
   - `ACCOUNT_SID`
   - `AUTH_TOKEN`
   - `TWILIO_PHONE_NUMBER`
3. Add Twilio dependencies in `pom.xml`:
   <dependency>
       <groupId>com.twilio.sdk</groupId>
       <artifactId>twilio</artifactId>
       <version>8.31.1</version>
   </dependency>
4. Add the details in application properties
twilio.account.sid=YOUR_ACCOUNT_SID
twilio.auth.token=YOUR_AUTH_TOKEN
twilio.phone.number=+1XXXXXXXXXX
