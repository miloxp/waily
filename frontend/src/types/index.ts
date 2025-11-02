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

export enum UserRole {
  PLATFORM_ADMIN = "PLATFORM_ADMIN",
  BUSINESS_OWNER = "BUSINESS_OWNER",
  BUSINESS_STAFF = "BUSINESS_STAFF",
}

export interface Subscription {
  id: string;
  businessId: string;
  businessName?: string;
  plan: SubscriptionPlan;
  status: SubscriptionStatus;
  startDate: string;
  endDate?: string;
  billingCycleDays: number;
  monthlyPrice: number;
  autoRenew: boolean;
  trialEndDate?: string;
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

export enum SubscriptionPlan {
  BASIC = "BASIC",
  PRO = "PRO",
  ENTERPRISE = "ENTERPRISE",
}

export enum SubscriptionStatus {
  ACTIVE = "ACTIVE",
  TRIAL = "TRIAL",
  EXPIRED = "EXPIRED",
  CANCELLED = "CANCELLED",
  SUSPENDED = "SUSPENDED",
}

export interface User {
  id: string;
  username: string;
  email: string;
  role: UserRole;
  businessIds: string[];
  businessNames?: string[];
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
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

