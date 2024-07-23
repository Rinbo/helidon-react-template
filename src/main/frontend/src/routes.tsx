import { LoaderFunction, LoaderFunctionArgs, redirect, RouteObject } from "react-router-dom";
import MainLayout from "./views/main-layout.tsx";
import { authProvider } from "./auth/auth.ts";
import Landing from "./views/landing.tsx";
import UsersView from "./views/about/users-view.tsx";
import LoginView, { action as loginAction } from "./views/login/login-view.tsx";
import RegistrationView, { action as registrationAction } from "./views/registration/registraiton-view.tsx";

export type MenuItem = Handle & { path: string };
type Handle = { requireAuth: boolean; label: string; icon: string; showInMenu: boolean };

export const routes: RouteObject[] = [
  {
    id: "root",
    path: "/",
    element: <MainLayout />,
    async loader() {
      await authProvider.fetchPrincipal();
      return { principal: authProvider.principal };
    },
    children: [
      {
        index: true,
        element: <Landing />,
        handle: { requireAuth: false, label: "Home", icon: "home", showInMenu: true },
      },
      {
        path: "/about",
        handle: { requireAuth: false, label: "About", icon: "info", showInMenu: true },
        element: <div className="py-4 text-center text-3xl uppercase">About</div>,
      },
      {
        path: "/users",
        loader: (args: LoaderFunctionArgs<any>) => requireAuth(args, nullLoader),
        handle: { requireAuth: true, label: "Users", icon: "users", showInMenu: true },
        element: <UsersView />,
      },
      {
        path: "/profile",
        element: <div className="py-4 text-center text-3xl uppercase">Profile</div>,
        handle: { requireAuth: true, label: "Profile", icon: "user", showInMenu: true },
      },
      {
        path: "/login",
        element: <LoginView />,
        loader: redirectIfAuthenticated,
        action: loginAction,
      },
      {
        path: "/register",
        loader: redirectIfAuthenticated,
        element: <RegistrationView />,
        action: registrationAction,
      },
    ],
  },
  {
    path: "/logout",
    async action() {
      await authProvider.logout();
      return redirect("/");
    },
  },
];

export const menuItems = routes
  .flatMap((route) => route.children ?? route)
  .filter((route) => route.handle)
  .filter((route) => route.handle?.showInMenu)
  .map((route) => ({ path: route.path ?? "/", ...route.handle })) as MenuItem[];

export async function requireAuth(args: LoaderFunctionArgs, loader: LoaderFunction) {
  const isAuthenticated = await authProvider.isAuthenticated();
  if (!isAuthenticated) return redirect(`/login`);
  return loader(args);
}

export async function redirectIfAuthenticated() {
  if (await authProvider.isAuthenticated()) return redirect("/");
  return null;
}

async function nullLoader(_args: LoaderFunctionArgs) {
  return null;
}
