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
