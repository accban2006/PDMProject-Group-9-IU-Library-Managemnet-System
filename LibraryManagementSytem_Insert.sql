USE [LibraryManagementSystemGroup9];
GO

-- =====================
-- 1. Insert Readers
-- =====================
INSERT INTO auth.Reader (FirstName, MiddleName, LastName, Address, DateOfBirth, Sex, Email, PhoneNumber)
VALUES 
('Nguyen', 'Van', 'An', '123 Le Loi, HN', '2000-01-15', 'M', 'nguyenvan.an@gmail.com', '0987654321'),
('Tran', 'Thi', 'B', '456 Tran Phu, HCM', '2002-05-22', 'F', 'tranthib@gmail.com', '0912345678');
GO

-- =====================
-- 2. Insert Staff
-- =====================
INSERT INTO auth.Staff (StaffName, StaffPhone, Email, Rank)
VALUES
(N'Huỳnh Ngọc Thủy Tiên', '0123456789', 'hnttien123@gmail.com', 1),
(N'Nguyễn Đức Huy', '0234567891', 'ndhuy123@gmail.com', 2),
(N'Nguyễn Chí Bảo', '0345678912', 'ncbao123@gmail.com', 3),
(N'Trần Minh Phúc', '0456789123', 'tmphuc123@gmail.com', 4);
GO

-- =====================
-- 3. Insert Publishers
-- =====================
INSERT INTO lib.Publisher (PublisherName)
VALUES
(N'NXB Kim Đồng'),
(N'NXB Trẻ'),
(N'NXB Dân Trí');
GO

-- =====================
-- 4. Insert Books
-- =====================
INSERT INTO lib.Book (ISBN, title, genre, edition, plot, ratings, StaffID, YearOfPublication, PublisherID)
VALUES
('978-1234567890', N'Lập Trình C#', 'Programming', '1st', 'A beginner book for C#.', 4.5, 1, 2015, 1),
('978-0987654321', N'Giải Tích 1', 'Mathematics', '2nd', 'Calculus textbook.', 4.0, 2, 2018,2);
GO

-- =====================
-- 5. Insert Authentication
-- =====================
INSERT INTO auth.Authentication (Username, Password, Displayname, ReaderID, StaffID, Role)
VALUES
('an123', '12345678', 'An Nguyen', 1, NULL,'Reader'),
('btran', 'password1', 'B Tran', 2, NULL,'Reader'),
('userstaff', 'userstaffpass', 'ThuyTien', NULL, 1, 'Staff'),
('hrstaff', 'hrstaffpass', 'DucHuy', NULL, 2, 'Staff'),
('financestaff', 'financestaffpass', 'ChiBao', NULL, 3, 'Staff'),
('bookstaff', 'bookstaffpass', 'PhucTran', NULL, 4, 'Staff');
GO

-- =====================
-- 6. Insert Messages
-- =====================
INSERT INTO operate.Message (MessNo, MessInfo, ReaderID)
VALUES
(1, 'I want to borrow "Lap Trinh C#".', 1),
(2, 'Is "Giai Tich 1" available?', 2);
GO

-- =====================
-- 7. Insert Reports
-- =====================
INSERT INTO operate.Report (ReportNo, Issue, StaffID)
VALUES
(1, 'Book "Lap Trinh C#" missing pages.', 1),
(2, 'Printer in library not working.', 2);
GO

-- =====================
-- 8. Insert Handle
-- =====================
INSERT INTO operate.Handle (StaffID, MessID, ReceiveDate, Feedback)
VALUES
(1, 1, '2025-11-18', 'Book reserved for you.'),
(2, 2, '2025-11-18', 'Book is available.');
GO

-- =====================
-- 9. Insert Loan
-- =====================
INSERT INTO loan.Loan (ReaderID, ISBN, BorrowDate, ReturnDate)
VALUES
(1, '978-1234567890', '2025-11-15', '2025-11-22'),
(2, '978-0987654321', '2025-11-16', '2025-11-23');
GO
