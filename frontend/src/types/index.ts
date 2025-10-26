export interface Business {
  id: string;
  name: string;
  type: BusinessType;
  address?: string;
  phone?: string;
  email?: string;
  capacity: number;
  averageServiceTime: number;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export enum BusinessType {
  RESTAURANT = "RESTAURANT",
  BAR = "BAR",
  CAFE = "CAFE",
  SALON = "SALON",
  SPA = "SPA",
  OTHER = "OTHER",
}

export interface Customer {
  id: string;
  phone: string;
  name?: string;
  email?: string;
  createdAt: string;
  updatedAt: string;
}

export interface Reservation {
  id: string;
  businessId: string;
  customerId: string;
  reservationDate: string;
  reservationTime: string;
  partySize: number;
  status: ReservationStatus;
  specialRequests?: string;
  createdAt: string;
  updatedAt: string;
  businessName?: string;
  customerName?: string;
  customerPhone?: string;
}

export enum ReservationStatus {
  PENDING = "PENDING",
  CONFIRMED = "CONFIRMED",
  CANCELLED = "CANCELLED",
  COMPLETED = "COMPLETED",
}

export interface WaitlistEntry {
  id: string;
  businessId: string;
  customerId: string;
  partySize: number;
  estimatedWaitTime?: number;
  position: number;
  status: WaitlistStatus;
  notifiedAt?: string;
  seatedAt?: string;
  createdAt: string;
  updatedAt: string;
  businessName?: string;
  customerName?: string;
  customerPhone?: string;
}

export enum WaitlistStatus {
  WAITING = "WAITING",
  NOTIFIED = "NOTIFIED",
  SEATED = "SEATED",
  CANCELLED = "CANCELLED",
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  type: string;
  username: string;
  roles: string;
}

export interface ApiError {
  message: string;
  status: number;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

