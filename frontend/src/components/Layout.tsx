import { ReactNode, useState } from "react";
import { Link, useLocation } from "react-router-dom";
import {
  Home,
  Building2,
  Calendar,
  Users,
  UserCheck,
  Menu,
  X,
  LogOut,
  CreditCard,
} from "lucide-react";
import { useAuth } from "../hooks/useAuth";
import { UserRole } from "../types";
import { clsx } from "clsx";

interface LayoutProps {
  children: ReactNode;
}

const platformAdminNavigation = [
  { name: "Panel de Plataforma", href: "/platform-admin", icon: Home },
  { name: "Negocios", href: "/businesses", icon: Building2 },
  { name: "Usuarios", href: "/users", icon: Users },
  { name: "Suscripciones", href: "/subscriptions", icon: CreditCard },
];

const businessNavigation = [
  { name: "Panel Principal", href: "/dashboard", icon: Home },
  { name: "Reservaciones", href: "/reservations", icon: Calendar },
  { name: "Lista de Espera", href: "/waitlist", icon: Users },
  { name: "Clientes", href: "/customers", icon: UserCheck },
  { name: "Usuarios", href: "/users", icon: Users },
];

const staffNavigation = [
  { name: "Panel Principal", href: "/dashboard", icon: Home },
  { name: "Reservaciones", href: "/reservations", icon: Calendar },
  { name: "Lista de Espera", href: "/waitlist", icon: Users },
  { name: "Clientes", href: "/customers", icon: UserCheck },
];

function getNavigation(role: UserRole | null) {
  if (role === UserRole.PLATFORM_ADMIN) {
    return platformAdminNavigation;
  } else if (role === UserRole.BUSINESS_OWNER) {
    return businessNavigation;
  } else {
    return staffNavigation;
  }
}

function getRoleLabel(role: UserRole | null): string {
  switch (role) {
    case UserRole.PLATFORM_ADMIN:
      return "Administrador de Plataforma";
    case UserRole.BUSINESS_OWNER:
      return "Propietario";
    case UserRole.BUSINESS_STAFF:
      return "Personal";
    default:
      return "Usuario";
  }
}

export default function Layout({ children }: LayoutProps) {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const location = useLocation();
  const { logout, role, user } = useAuth();
  const navigation = getNavigation(role);

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Mobile sidebar */}
      <div
        className={clsx(
          "fixed inset-0 z-50 lg:hidden",
          sidebarOpen ? "block" : "hidden"
        )}
      >
        <div
          className="fixed inset-0 bg-gray-600 bg-opacity-75"
          onClick={() => setSidebarOpen(false)}
        />
        <div className="fixed inset-y-0 left-0 flex w-64 flex-col bg-white">
          <div className="flex h-16 items-center justify-between px-4">
            <div>
            <h1 className="text-xl font-bold text-gray-900">
              Lista de Espera y Reservaciones
            </h1>
              {role && (
                <p className="text-xs text-gray-500">{getRoleLabel(role)}</p>
              )}
            </div>
            <button
              onClick={() => setSidebarOpen(false)}
              className="text-gray-400 hover:text-gray-600"
            >
              <X className="h-6 w-6" />
            </button>
          </div>
          <nav className="flex-1 space-y-1 px-2 py-4">
            {navigation.map((item) => {
              const isActive = location.pathname === item.href;
              return (
                <Link
                  key={item.name}
                  to={item.href}
                  className={clsx(
                    "group flex items-center px-2 py-2 text-sm font-medium rounded-md",
                    isActive
                      ? "bg-primary-100 text-primary-900"
                      : "text-gray-600 hover:bg-gray-50 hover:text-gray-900"
                  )}
                  onClick={() => setSidebarOpen(false)}
                >
                  <item.icon
                    className={clsx(
                      "mr-3 h-5 w-5 flex-shrink-0",
                      isActive
                        ? "text-primary-500"
                        : "text-gray-400 group-hover:text-gray-500"
                    )}
                  />
                  {item.name}
                </Link>
              );
            })}
          </nav>
          <div className="border-t border-gray-200 p-4">
            <button
              onClick={logout}
              className="flex w-full items-center px-2 py-2 text-sm font-medium text-gray-600 hover:bg-gray-50 hover:text-gray-900 rounded-md"
            >
              <LogOut className="mr-3 h-5 w-5 text-gray-400" />
              Cerrar Sesión
            </button>
          </div>
        </div>
      </div>

      {/* Desktop sidebar */}
      <div className="hidden lg:fixed lg:inset-y-0 lg:flex lg:w-64 lg:flex-col">
        <div className="flex flex-col flex-grow bg-white border-r border-gray-200">
          <div className="flex h-16 items-center px-4">
            <div>
            <h1 className="text-xl font-bold text-gray-900">
              Lista de Espera y Reservaciones
            </h1>
              {role && (
                <p className="text-xs text-gray-500">{getRoleLabel(role)}</p>
              )}
            </div>
          </div>
          <nav className="flex-1 space-y-1 px-2 py-4">
            {navigation.map((item) => {
              const isActive = location.pathname === item.href;
              return (
                <Link
                  key={item.name}
                  to={item.href}
                  className={clsx(
                    "group flex items-center px-2 py-2 text-sm font-medium rounded-md",
                    isActive
                      ? "bg-primary-100 text-primary-900"
                      : "text-gray-600 hover:bg-gray-50 hover:text-gray-900"
                  )}
                >
                  <item.icon
                    className={clsx(
                      "mr-3 h-5 w-5 flex-shrink-0",
                      isActive
                        ? "text-primary-500"
                        : "text-gray-400 group-hover:text-gray-500"
                    )}
                  />
                  {item.name}
                </Link>
              );
            })}
          </nav>
          <div className="border-t border-gray-200 p-4">
            <button
              onClick={logout}
              className="flex w-full items-center px-2 py-2 text-sm font-medium text-gray-600 hover:bg-gray-50 hover:text-gray-900 rounded-md"
            >
              <LogOut className="mr-3 h-5 w-5 text-gray-400" />
              Cerrar Sesión
            </button>
          </div>
        </div>
      </div>

      {/* Main content */}
      <div className="lg:pl-64">
        {/* Top bar */}
        <div className="sticky top-0 z-40 flex h-16 shrink-0 items-center gap-x-4 border-b border-gray-200 bg-white px-4 shadow-sm sm:gap-x-6 sm:px-6 lg:px-8">
          <button
            type="button"
            className="-m-2.5 p-2.5 text-gray-700 lg:hidden"
            onClick={() => setSidebarOpen(true)}
          >
            <Menu className="h-6 w-6" />
          </button>
          <div className="flex flex-1 gap-x-4 self-stretch lg:gap-x-6">
            <div className="flex flex-1" />
            <div className="flex items-center gap-x-4 lg:gap-x-6">
              <div className="hidden lg:block lg:h-6 lg:w-px lg:bg-gray-200" />
              <div className="flex items-center gap-x-2">
                {user && role && (
                  <div className="text-right">
                    <span className="text-sm font-medium text-gray-900">
                      {user.username}
                </span>
                    <p className="text-xs text-gray-500">
                      {getRoleLabel(role)}
                    </p>
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>

        {/* Page content */}
        <main className="py-6">
          <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
            {children}
          </div>
        </main>
      </div>
    </div>
  );
}
