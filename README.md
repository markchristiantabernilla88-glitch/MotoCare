# MotoCare
# MotoCare: A Vehicle Maintenance Appointment Scheduling and Reservation System
## 1. Project Title

**MotoCare: A Vehicle Maintenance Appointment Scheduling and Reservation System**

## 2. Project Description

MotoCare is a Java-based desktop application designed to make vehicle maintenance reservations easier and more organized. It allows customers to view available maintenance services, check estimated prices, and reserve an appointment based on their preferred date and time.

The system also provides a Shop Staff dashboard where staff can manage reservations, update appointment statuses, monitor daily schedules and reservation capacity, and generate reservation reports.

## 3. Problem Being Addressed

Vehicle owners often experience long waiting times when visiting maintenance shops because of the number of customers requesting services. This can result in overcrowding, scheduling conflicts, and situations where customers cannot be accommodated due to the shop's daily service limit.

MotoCare addresses this problem by allowing customers to schedule their maintenance appointments in advance. It also helps shop staff organize reservations, monitor available appointment slots, and manage daily customer capacity.

## 4. Project Objectives

The main objectives of MotoCare are:

- To provide a convenient way for customers to reserve vehicle maintenance appointments.
- To reduce waiting times and avoid unnecessary queues at maintenance shops.
- To display maintenance services and estimated costs based on vehicle types.
- To prevent overbooking through daily reservation limits and time slot availability.
- To help shop staff organize and manage customer reservations.
- To generate reservation reports containing service costs and labor charges.

## 5. Target Users

**Customer**
- Create an account and log in to the system.
- View available maintenance services and estimated prices.
- Select their vehicle type and category.
- Submit maintenance reservations.
- View their appointment details and reservation status.

**Shop Staff**
- Log in using the staff account.
- View and manage all customer reservations.
- Approve, reject, or set reservations to pending.
- View the daily maintenance schedule.
- Monitor daily reservation capacity.
- Generate and export reservation reports.

## 6. Main Features

### Account Management
- Customer registration and login.
- Separate dashboards for customers and shop staff.
- Account information is saved for future use.

### Maintenance Services
- Oil Change
- Brake Pad Replacement
- Engine Repair
- Tire Service
- General Vehicle Inspection

### Vehicle Classification
- Motorcycle (Lower CC and Big Bike)
- Car (Sedan, SUV, Pickup, Sports Car, Van, and Others)
- Estimated service costs and labor charges are adjusted according to the selected vehicle category.

### Maintenance Reservation
- Enter vehicle model and year.
- Select a maintenance service.
- Choose a preferred appointment date and time.
- View estimated cost, labor, and total price.
- Receive confirmation that the reservation has been submitted as Pending.

### Reservation Availability
- Daily reservation limit of 30 appointments.
- Maximum of 2 active reservations per time slot.
- Appointment schedules are available every 30 minutes, from 8:00 AM to 5:00 PM.
- Automatically identifies fully booked time slots.
- Prevents reservations for past dates and elapsed time slots.

### Customer Appointments
- View personal reservation records.
- Check appointment dates, times, vehicle information, services, and reservation statuses.

### Staff Reservation Management
- View all customer reservations.
- Approve reservations.
- Reject reservations.
- Change reservation status back to Pending.

### Daily Schedule and Capacity
- View reservations for a selected date.
- Navigate between previous and next days.
- Monitor the number of reservations and remaining daily capacity.

### Reservation Reports
- View reservation records based on their status.
- Calculate total service costs and labor charges.
- Display the grand total.
- Export reservation reports as CSV files.

## 7. Technologies Used

| Technology | Purpose |
|---|---|
| Java | Main programming language |
| Java Swing | Graphical User Interface (GUI) |
| Java AWT | Layouts, colors, fonts, and interface components |
| Java Collections Framework | Managing users, appointments, services, and vehicle types |
| Java Time API | Handling appointment dates and times |
| File Handling | Saving and loading user and reservation information |
| CSV | Exporting reservation reports |
| Visual Studio Code | Code editing and development |
| GitHub | Source code repository and collaboration |

## 8. Team Members and Roles

| Team Member | Assigned Role |
|---|---|
| Cabral, Vince Rovick E. | Documentation |
| Llamando, Godwin R. | Programmer / GUI Designer |
| Tabernilla, Mark Christian D. | Project Leader / Programmer |

## 9. OOP Concepts Demonstrated

**Encapsulation**

The system uses private fields inside classes such as User, VehicleType, ServiceItem, and Appointment. Getter and setter methods are used to access or modify selected information.

**Inheritance**

The Customer and ShopStaff classes inherit common attributes and methods from the abstract User class. This avoids repeating the same user information in different classes.

**Polymorphism**

The Customer and ShopStaff classes override the getRole() method inherited from User. Each class provides its own implementation depending on the user's role.

**Abstraction**

The User class is declared abstract and contains the abstract method getRole(). It provides a common structure for different types of users while allowing subclasses to implement their own roles.

**Exception Handling**

The program uses try-catch statements to handle errors involving invalid date formats, file reading and writing, and other possible runtime errors.

**Arrays and Data Handling**

The program uses arrays and Java collections such as ArrayList, List, and TreeSet to organize and manage user accounts, appointments, vehicle categories, and maintenance services.

## 10. Instructions for Running the Program

### Requirements
- Java Development Kit (JDK 17 or later recommended).
- Visual Studio Code or another Java-compatible IDE.

### Steps to Run

1. Download or clone the MotoCare project repository from GitHub.
2. Open the project folder in Visual Studio Code.
3. Make sure Java JDK is properly installed.
4. Open the terminal inside the project folder.
5. Compile the Java source code using:

   ```bash
   javac MotoCare.java
   ```

6. Run the program using:

   ```bash
   java MotoCare
   ```

7. The MotoCare login screen will appear.

### Default Shop Staff Account

| Field | Account Details |
|---|---|
| Username | staff |
| Password | staff123 |

Customers can also create their own accounts through the Create Account option.

## 11. Sample Screenshots

The following screenshots should be included to demonstrate the actual system interface:

1. Login Screen
2. Create Account Screen
3. Customer Dashboard
4. Maintenance Services Screen
5. Make a Reservation Screen
6. My Appointments Screen
7. Shop Staff Dashboard
8. Manage Reservations Screen
9. Daily Schedule Screen
10. Daily Capacity Screen
11. Reservation Report Screen

*Insert the actual screenshots of the running MotoCare application under this section.*

## 12. Known Limitations

- The system uses local text file storage instead of an online database.
- The application is designed for local desktop use and does not support online access.
- Only the predefined maintenance services and vehicle categories are available.
- The system uses fixed daily reservation limits and appointment time slots.
- The default Shop Staff account is provided for demonstration and testing purposes.
- The application does not include online payment processing.
- Passwords stored in the local data file are encoded but are not securely encrypted.

## 13. Future Improvements

- Integrate a database such as MySQL for more advanced data management.
- Add online payment functionality.
- Provide email or SMS notifications for reservation updates.
- Add more vehicle categories and maintenance services.
- Improve account security through password hashing.
- Develop an online or mobile version of MotoCare.
- Add additional reporting and reservation analytics.

## 14. Project Contributors

This project was developed by the members of the MotoCare project team as part of the Object-Oriented Programming (OOP 101) course.

**Project:** MotoCare: A Vehicle Maintenance Appointment Scheduling and Reservation System

**Repository:** [https://github.com/markchristiantabernilla88-glitch/MotoCare]

**Course:** Object-Oriented Programming (OOP 101)

**Programming Language:** Java

## GitHub Contribution

Each team member is expected to contribute to the development of the project repository.

Contributions may include:

- Writing and improving the Java source code.
- Designing and improving the graphical user interface.
- Testing system functionalities and fixing errors.
- Preparing project documentation.
- Updating the README.md file.
- Participating in project development and presentation.

All team members should make their contributions through GitHub commits to document their participation in the project.