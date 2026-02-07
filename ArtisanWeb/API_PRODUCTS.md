# Products API Documentation

## Base URL

`/api/v1/products`

## Endpoints

### GET /api/v1/products

Get all products with optional filtering.

**Query Parameters:**

- `page` (optional, default: 0) - Page number
- `size` (optional, default: 20) - Page size
- `category` (optional) - Filter by category
- `search` (optional) - Search in name and description
- `sellerId` (optional) - Filter by seller ID
- `availableOnly` (optional, default: false) - Filter only available products

**Response:**

```json
{
  "content": [
    {
      "id": 1,
      "name": "Product name",
      "description": "Product description",
      "price": 150.0,
      "imageUrl": "https://...",
      "sellerId": 1,
      "sellerName": "Seller name",
      "category": "Category",
      "createdAt": "2024-01-15T10:30:00Z",
      "isAvailable": true
    }
  ],
  "totalElements": 50,
  "totalPages": 3,
  "page": 0,
  "size": 20
}
```

### GET /api/v1/products/{id}

Get product by ID.

**Response:**

```json
{
  "id": 1,
  "name": "Product name",
  "description": "Product description",
  "price": 150.0,
  "imageUrl": "https://...",
  "sellerId": 1,
  "sellerName": "Seller name",
  "category": "Category",
  "createdAt": "2024-01-15T10:30:00Z",
  "isAvailable": true
}
```

### POST /api/v1/products

Create a new product. Requires authentication.

**Request Body:**

```json
{
  "name": "Product name",
  "description": "Product description",
  "price": 150.0,
  "category": "Category",
  "imageUrl": "https://..."
}
```

**Response:** 201 Created with product data

### PUT /api/v1/products/{id}

Update product. Requires authentication (owner only).

**Request Body:**

```json
{
  "name": "Updated name",
  "description": "Updated description",
  "price": 200.0,
  "category": "Updated category",
  "isAvailable": true
}
```

### DELETE /api/v1/products/{id}

Delete product. Requires authentication (owner only).

**Response:** 204 No Content

### POST /api/v1/products/{id}/cart

Add product to cart. Requires authentication.

**Response:** 200 OK

### POST /api/v1/products/{id}/buy

Purchase product directly. Requires authentication.

**Response:** 200 OK with order details

### POST /api/v1/products/{id}/contact

Start chat with seller. Requires authentication.

**Response:** 200 OK with chat/thread details

## Categories

- Деревянные изделия (Woodwork)
- Керамика (Ceramics)
- Текстиль (Textile)
- Стекло (Glass)
- Кожаные изделия (Leather goods)
- Свечи (Candles)
