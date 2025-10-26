import axios, { AxiosInstance, AxiosResponse } from "axios";
import {
  Business,
  Customer,
  Reservation,
  WaitlistEntry,
  LoginRequest,
  LoginResponse,
} from "../types";

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api";

class ApiService {
  private api: AxiosInstance;

  constructor() {
    this.api = axios.create({
      baseURL: API_BASE_URL,
      headers: {
        "Content-Type": "application/json",
      },
    });

    // Add request interceptor to include auth token
    this.api.interceptors.request.use(
      (config) => {
        const token = localStorage.getItem("token");
        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
      },
      (error) => {
        return Promise.reject(error);
      }
    );

    // Add response interceptor to handle errors
    this.api.interceptors.response.use(
      (response) => response,
      (error) => {
        if (error.response?.status === 401) {
          localStorage.removeItem("token");
          window.location.href = "/login";
        }
        return Promise.reject(error);
      }
    );
  }

  // Auth endpoints
  async login(credentials: LoginRequest): Promise<LoginResponse> {
    const response: AxiosResponse<LoginResponse> = await this.api.post(
      "/auth/login",
      credentials
    );
    return response.data;
  }

  async validateToken(): Promise<boolean> {
    try {
      const token = localStorage.getItem("token");
      if (!token) return false;

      // Simple JWT token validation - check if it's not expired
      const payload = JSON.parse(atob(token.split(".")[1]));
      const currentTime = Date.now() / 1000;
      return payload.exp > currentTime;
    } catch {
      return false;
    }
  }

  async register(registerData: any): Promise<LoginResponse> {
    const response: AxiosResponse<LoginResponse> = await this.api.post(
      "/auth/register",
      registerData
    );
    return response.data;
  }

  async getProfile(): Promise<any> {
    const response = await this.api.get("/auth/profile");
    return response.data;
  }

  // Business endpoints
  async getBusinesses(): Promise<Business[]> {
    const response: AxiosResponse<Business[]> = await this.api.get("/business");
    return response.data;
  }

  async getBusiness(id: string): Promise<Business> {
    const response: AxiosResponse<Business> = await this.api.get(
      `/business/${id}`
    );
    return response.data;
  }

  async createBusiness(
    business: Omit<Business, "id" | "createdAt" | "updatedAt">
  ): Promise<Business> {
    const response: AxiosResponse<Business> = await this.api.post(
      "/business",
      business
    );
    return response.data;
  }

  async updateBusiness(
    id: string,
    business: Partial<Business>
  ): Promise<Business> {
    const response: AxiosResponse<Business> = await this.api.put(
      `/business/${id}`,
      business
    );
    return response.data;
  }

  async deleteBusiness(id: string): Promise<void> {
    await this.api.delete(`/business/${id}`);
  }

  async searchBusinesses(searchTerm: string): Promise<Business[]> {
    const response: AxiosResponse<Business[]> = await this.api.get(
      `/business/search?searchTerm=${searchTerm}`
    );
    return response.data;
  }

  // Customer endpoints
  async getCustomers(): Promise<Customer[]> {
    const response: AxiosResponse<Customer[]> = await this.api.get(
      "/customers"
    );
    return response.data;
  }

  async getCustomer(id: string): Promise<Customer> {
    const response: AxiosResponse<Customer> = await this.api.get(
      `/customers/${id}`
    );
    return response.data;
  }

  async getCustomerByPhone(phone: string): Promise<Customer> {
    const response: AxiosResponse<Customer> = await this.api.get(
      `/customers/phone/${phone}`
    );
    return response.data;
  }

  async createCustomer(
    customer: Omit<Customer, "id" | "createdAt" | "updatedAt">
  ): Promise<Customer> {
    const response: AxiosResponse<Customer> = await this.api.post(
      "/customers",
      customer
    );
    return response.data;
  }

  async updateCustomer(
    id: string,
    customer: Partial<Customer>
  ): Promise<Customer> {
    const response: AxiosResponse<Customer> = await this.api.put(
      `/customers/${id}`,
      customer
    );
    return response.data;
  }

  async findOrCreateCustomer(
    customer: Omit<Customer, "id" | "createdAt" | "updatedAt">
  ): Promise<Customer> {
    const response: AxiosResponse<Customer> = await this.api.post(
      "/customers/find-or-create",
      customer
    );
    return response.data;
  }

  async searchCustomers(searchTerm: string): Promise<Customer[]> {
    const response: AxiosResponse<Customer[]> = await this.api.get(
      `/customers/search?searchTerm=${searchTerm}`
    );
    return response.data;
  }

  // Reservation endpoints
  async getReservations(): Promise<Reservation[]> {
    const response: AxiosResponse<Reservation[]> = await this.api.get(
      "/reservations"
    );
    return response.data;
  }

  async getReservation(id: string): Promise<Reservation> {
    const response: AxiosResponse<Reservation> = await this.api.get(
      `/reservations/${id}`
    );
    return response.data;
  }

  async getReservationsByBusiness(
    businessId: string,
    date?: string
  ): Promise<Reservation[]> {
    const url = date
      ? `/reservations/business/${businessId}?date=${date}`
      : `/reservations/business/${businessId}`;
    const response: AxiosResponse<Reservation[]> = await this.api.get(url);
    return response.data;
  }

  async getReservationsByCustomer(customerId: string): Promise<Reservation[]> {
    const response: AxiosResponse<Reservation[]> = await this.api.get(
      `/reservations/customer/${customerId}`
    );
    return response.data;
  }

  async createReservation(
    reservation: Omit<Reservation, "id" | "createdAt" | "updatedAt">
  ): Promise<Reservation> {
    const response: AxiosResponse<Reservation> = await this.api.post(
      "/reservations",
      reservation
    );
    return response.data;
  }

  async confirmReservation(id: string): Promise<Reservation> {
    const response: AxiosResponse<Reservation> = await this.api.put(
      `/reservations/${id}/confirm`
    );
    return response.data;
  }

  async cancelReservation(id: string): Promise<Reservation> {
    const response: AxiosResponse<Reservation> = await this.api.put(
      `/reservations/${id}/cancel`
    );
    return response.data;
  }

  async completeReservation(id: string): Promise<Reservation> {
    const response: AxiosResponse<Reservation> = await this.api.put(
      `/reservations/${id}/complete`
    );
    return response.data;
  }

  // Waitlist endpoints
  async getWaitlistByBusiness(businessId: string): Promise<WaitlistEntry[]> {
    const response: AxiosResponse<WaitlistEntry[]> = await this.api.get(
      `/waitlist/business/${businessId}`
    );
    return response.data;
  }

  async getWaitlistEntry(id: string): Promise<WaitlistEntry> {
    const response: AxiosResponse<WaitlistEntry> = await this.api.get(
      `/waitlist/${id}`
    );
    return response.data;
  }

  async addToWaitlist(
    entry: Omit<WaitlistEntry, "id" | "createdAt" | "updatedAt">
  ): Promise<WaitlistEntry> {
    const response: AxiosResponse<WaitlistEntry> = await this.api.post(
      "/waitlist",
      entry
    );
    return response.data;
  }

  async notifyCustomer(id: string): Promise<WaitlistEntry> {
    const response: AxiosResponse<WaitlistEntry> = await this.api.put(
      `/waitlist/${id}/notify`
    );
    return response.data;
  }

  async seatCustomer(id: string): Promise<WaitlistEntry> {
    const response: AxiosResponse<WaitlistEntry> = await this.api.put(
      `/waitlist/${id}/seat`
    );
    return response.data;
  }

  async cancelWaitlistEntry(id: string): Promise<WaitlistEntry> {
    const response: AxiosResponse<WaitlistEntry> = await this.api.put(
      `/waitlist/${id}/cancel`
    );
    return response.data;
  }

  async getWaitlistStats(
    businessId: string
  ): Promise<{ waitingCount: number; activeCount: number }> {
    const response = await this.api.get(
      `/waitlist/business/${businessId}/stats`
    );
    return response.data;
  }

  // Notification endpoints
  async sendSms(customerId: string, message: string): Promise<any> {
    const response = await this.api.post("/notifications/sms", {
      customerId,
      message,
    });
    return response.data;
  }

  async checkSmsStatus(messageId: string): Promise<any> {
    const response = await this.api.get(`/notifications/status/${messageId}`);
    return response.data;
  }

  // Public endpoints
  async getPublicWaitlistInfo(businessId: string): Promise<any> {
    const response = await this.api.get(`/public/waitlist/${businessId}`);
    return response.data;
  }
}

export const apiService = new ApiService();
