CREATE DATABASE [LibraryManagementSystemGroup9];
GO
USE [LibraryManagementSystemGroup9];
GO
CREATE SCHEMA auth;
GO
CREATE TABLE auth.Authentication (
	[Username] VARCHAR(20) NOT NULL,
	[Password] VARCHAR(64) NOT NULL,
	[Displayname] VARCHAR(20) NOT NULL,
	[ReaderID] INT,
	[StaffID] INT,
	[Role] VARCHAR(20) NULL
);
GO

CREATE TABLE auth.Reader (
	[ReaderID] INT IDENTITY(1,1) NOT NULL,
	[FirstName] NVARCHAR(25) NULL,
	[MiddleName] NVARCHAR(25) NULL,
	[LastName] NVARCHAR(25) NULL,
	[Address] NVARCHAR(50) NULL,
	[DateOfBirth] DATE NULL,
	[Sex] VARCHAR(1) NULL,
	[Email] VARCHAR(100) NULL,
	[PhoneNumber] VARCHAR(15) NULL
);
GO

CREATE TABLE auth.Staff (
	[StaffID] INT IDENTITY(1,1) NOT NULL,
	[StaffName] NVARCHAR(100) NULL,
	[StaffPhone] VARCHAR(15) NULL,
	[Email] VARCHAR(100) NULL,
	[Rank] INT NULL
);
GO

CREATE SCHEMA lib;
GO

CREATE TABLE lib.Book (
	[ISBN] VARCHAR(50) NOT NULL,
	[title] NVARCHAR(50) NULL,
	[genre] VARCHAR(50) NULL,
	[edition] VARCHAR(50) NULL,
	[plot] NVARCHAR(MAX) NULL,
	[ratings] DECIMAL(2,1) NULL,
	[StaffID] INT NULL,
	[YearOfPublication] INT NULL,
	[PublisherID] INT NULL
);
GO

CREATE TABLE lib.Publisher (
	[PublisherID] INT IDENTITY(1,1) NOT NULL,
	[PublisherName] NVARCHAR(50) NULL
);
GO

CREATE SCHEMA operate;
GO

CREATE TABLE operate.Message (
	[MessID] INT IDENTITY(1,1) NOT NULL,
	[MessNo] INT NOT NULL,
	[MessInfo] NVARCHAR(1000) NOT NULL,
	[ReaderID] INT NOT NULL
);
GO

CREATE TABLE operate.Report (
	[ReportID] INT IDENTITY(1,1) NOT NULL,
	[ReportNo] INT NOT NULL,
	[Issue] NVARCHAR(1000) NULL,
	[StaffID] INT NOT NULL
);
GO

CREATE TABLE operate.Handle (
	[StaffID] INT NOT NULL,
	[MessID] INT NOT NULL,
	[ReceiveDate] DATE NULL,
	[Feedback] NVARCHAR(1000) NULL
);
GO

CREATE SCHEMA loan;
GO

CREATE TABLE loan.Loan (
	[ReaderID] INT NOT NULL,
	[ISBN] VARCHAR(50) NOT NULL,
	[BorrowDate] DATE NULL,
	[ReturnDate] DATE NULL
	[Status] VARCHAR(20) DEFAULT 'pending'
);
GO

ALTER TABLE auth.Authentication ADD
	CONSTRAINT Password_Minimum_Length CHECK (LEN([Password]) >= 8),
	CONSTRAINT Default_Role DEFAULT('Reader') FOR [Role],
	CONSTRAINT Authentication_PK PRIMARY KEY
	(
		[Username]
	),
	CONSTRAINT Unique_Authentication CHECK (
		([ReaderID] IS NULL AND [StaffID] IS NOT NULL)
		OR ([ReaderID] IS NOT NULL AND [StaffID] IS NULL)
	);
GO

ALTER TABLE auth.Reader ADD
	CONSTRAINT ReaderID_PK PRIMARY KEY
	(
		[ReaderID]
	);
GO

ALTER TABLE auth.Staff ADD
	CONSTRAINT Rank_Default CHECK([Rank] >= 0),
	CONSTRAINT StaffID_PK PRIMARY KEY
	(
		[StaffID]
	);
GO

ALTER TABLE auth.Authentication ADD
	CONSTRAINT Authentication_FK00 FOREIGN KEY
	(
		[ReaderID]
	) REFERENCES auth.Reader (
		[ReaderID]
	),
	CONSTRAINT Authentication_FK01 FOREIGN KEY
	(
		[StaffID]
	) REFERENCES auth.Staff (
		[StaffID]
	);
GO

ALTER TABLE operate.Message ADD
	CONSTRAINT MessNo_Positive CHECK([MessNo] > 0),
	CONSTRAINT MessID_PK PRIMARY KEY
	(
		[MessID]
	);
GO

ALTER TABLE operate.Message ADD
	CONSTRAINT Message_FK00 FOREIGN KEY
	(
		[ReaderID]
	) REFERENCES auth.Reader (
		[ReaderID]
	);
GO

ALTER TABLE operate.Report ADD
	CONSTRAINT ReportNo_Positive CHECK([ReportNo] > 0),
	CONSTRAINT Report_PK PRIMARY KEY
	(
		[ReportID]
	);
GO

ALTER TABLE operate.Report ADD
	CONSTRAINT Report_FK00 FOREIGN KEY
	(
		[StaffID]
	) REFERENCES auth.Staff (
		[StaffID]
	);
GO

ALTER TABLE operate.Handle ADD
	CONSTRAINT Handle_PK PRIMARY KEY
	(
		[StaffID],
		[MessID]
	);
GO

ALTER TABLE operate.Handle ADD
	CONSTRAINT Handle_FK00 FOREIGN KEY
	(
		[StaffID]
	) REFERENCES auth.Staff (
		[StaffID]
	),
	CONSTRAINT Handle_FK01 FOREIGN KEY
	(
		[MessID]
	) REFERENCES operate.Message (
		[MessID]
	);
GO

ALTER TABLE lib.Book ADD
	CONSTRAINT Ratings_Non_Negative CHECK([ratings] BETWEEN 0 AND 5),
	CONSTRAINT YearOfPublication_Positive CHECK([YearOfPublication] > 0),
	CONSTRAINT ISBN_PK PRIMARY KEY 
	(
		[ISBN]
	);
GO

ALTER TABLE lib.Publisher ADD
	CONSTRAINT PublisherID_PK PRIMARY KEY
	(
		[PublisherID]
	);
GO

ALTER TABLE lib.Book ADD
	CONSTRAINT Book_FK00 FOREIGN KEY
	(
		[StaffID]
	) REFERENCES auth.Staff (
		[StaffID]
	),
	CONSTRAINT Book_FK01 FOREIGN KEY
	(
		[PublisherID]
	) REFERENCES lib.Publisher (
		[PublisherID]
	);
GO

ALTER TABLE loan.Loan ADD
	CONSTRAINT BorrowDate_ReturnDate CHECK([BorrowDate] <= [ReturnDate]),
	CONSTRAINT Loan_PK PRIMARY KEY
	(
		[ReaderID],
		[ISBN]
	);
GO

ALTER TABLE loan.Loan ADD
	CONSTRAINT Loan_FK00 FOREIGN KEY
	(
		[ReaderID]
	) REFERENCES auth.Reader (
		[ReaderID]
	),
	CONSTRAINT Loan_FK01 FOREIGN KEY
	(
		[ISBN]
	) REFERENCES lib.Book (
		[ISBN]
	);
GO
