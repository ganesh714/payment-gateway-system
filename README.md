# Payment Gateway System

A "Mini-Razorpay" style payment gateway with Merchant Onboarding, Order Management, and Hosted Checkout.

## Project Structure
- **backend**: Java Spring Boot API (Port 8000)
- **frontend**: React Dashboard (Port 3000)
- **checkout-page**: React Checkout Page (Port 3001)
- **postgres**: Database (Port 5432)

## Setup
### Prerequisites
- Docker and Docker Compose

### Running the Application
```bash
docker-compose up -d --build
```
Wait for all services to be healthy.

## Test Credentials
The system auto-seeds a test merchant:
- **Email**: `test@example.com`
- **API Key**: `key_test_abc123`
- **API Secret**: `secret_test_xyz789`

## Features
- **Merchant Dashboard**: View transactions and credentials at [http://localhost:3000](http://localhost:3000). (Login with `test@example.com` / any password)
- **Hosted Checkout**: Complete payments at [http://localhost:3001/checkout?order_id=...](http://localhost:3001/checkout).
- **Payment Processing**: Simulated 5-10s delay with random success/failure.
- **Validation**: Full Luhn algorithm, VPA validation, and synchronous processing.

## API Endpoints
- `GET /health` - System health
- `POST /api/v1/orders` - Create Order (Auth required)
- `POST /api/v1/payments` - Create Payment (Auth required)
- `GET /api/v1/payments/{id}` - Get Payment Status (Auth required)

## Verification Process (For Validators)
Follow these steps to validate the payment flow:

### 1. Create an Order
Use the following PowerShell command to create a test order:
```powershell
Invoke-RestMethod -Uri "http://localhost:8000/api/v1/orders" -Method Post -Headers @{"X-Api-Key"="key_test_abc123"; "X-Api-Secret"="secret_test_xyz789"; "Content-Type"="application/json"} -Body '{"amount": 75000, "currency": "INR", "receipt": "final_verification"}'
```
**Expected Output:**
- Returns an `id` (e.g., `order_yVWbPfcNrb5Rpkxt`)
- Status: `created`

### 2. Process Payment
Copy the `id` from the previous step and open the checkout page:
`http://localhost:3001/?order_id=YOUR_ORDER_ID`
(Example: `http://localhost:3001/?order_id=order_yVWbPfcNrb5Rpkxt`)

- Select a payment method (Card/UPI).
- Click "Pay Now".
- Wait for success/failure message.

### 3. Verify in Dashboard
1. Go to [http://localhost:3000](http://localhost:3000).
2. Login with `test@example.com` (any password).
3. Check **Transactions** tab to see the new entry.

