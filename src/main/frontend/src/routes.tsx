import { ActionFunction, ActionFunctionArgs, json, LoaderFunction, LoaderFunctionArgs, redirect, RouteObject } from "react-router-dom";
import MainLayout from "./views/main-layout.tsx";
import { authProvider } from "./auth/auth.ts";
import Landing from "./views/landing.tsx";
import UsersView from "./views/users/users-view.tsx";
import LoginView, { action as loginAction } from "./views/login/login-view.tsx";
import RegistrationView, { action as registrationAction } from "./views/registration/registraiton-view.tsx";
import toast from "react-hot-toast";
import ErrorBoundary from "./views/ErrorBoundary.tsx";
import UsersLayout, { loader as usersLoader, User } from "./views/users/users-layout.tsx";
import NewUserView, { action as newUserAction } from "./views/users/new-user-view.tsx";
import { isAdmin } from "./utils/http.ts";
import ShowUserView, { loader as showUserLoader } from "./views/users/show-user-view.tsx";

export type MenuItem = Handle & { path: string };
type Handle = { requireAuth: boolean; label: string; icon: string; showInMenu: boolean };

export const routes: RouteObject[] = [
  {
    id: "root",
    path: "/",
    element: <MainLayout />,
    errorElement: <ErrorBoundary />,
    async loader() {
      await authProvider.fetchPrincipal();
      return { principal: authProvider.principal };
    },
    children: [
      {
        index: true,
        element: <Landing />,
        handle: { requireAuth: false, label: "Home", icon: "home", showInMenu: true, crumb: () => "Home" },
      },
      {
        path: "/about",
        handle: { requireAuth: false, label: "About", icon: "info", showInMenu: true, crumb: () => "About" },
        element: <div className="py-4 text-center text-3xl uppercase">About</div>,
      },
      {
        path: "/users",
        loader: (args: LoaderFunctionArgs<any>) => requireAuth(args, usersLoader),
        handle: { requireAuth: true, label: "Users", icon: "users", showInMenu: true, crumb: () => "Users" },
        element: <UsersLayout />,
        children: [
          {
            index: true,
            element: <UsersView />,
            handle: { crumb: () => "All" },
          },
          {
            path: "new",
            element: <NewUserView />,
            action: (args: ActionFunctionArgs) => requireAdminAction(args, newUserAction),
            handle: { crumb: () => "Add" },
          },
          {
            path: ":userId",
            element: <ShowUserView />,
            loader: (args: LoaderFunctionArgs) => requireAuth(args, showUserLoader),
            handle: { crumb: (user: User) => user.name },
          },
        ],
      },
      {
        path: "/profile",
        element: <div className="py-4 text-center text-3xl uppercase">Profile</div>,
        handle: { requireAuth: true, label: "Profile", icon: "user", showInMenu: true, crumb: () => "Profile" },
      },
      {
        path: "/login",
        element: <LoginView />,
        loader: redirectIfAuthenticated,
        action: loginAction,
      },
      {
        path: "/authenticate",
        action: authenticationAction,
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
      toast.success("Goodbye, see you later!");
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

async function authenticationAction({ request }: ActionFunctionArgs) {
  try {
    await authProvider.authenticate(await request.json());
    toast.success("Welcome " + authProvider.principal?.name, { duration: 4000 });
    return redirect("/");
  } catch (error) {
    return json({ error: (error as Error)?.message || "Unknown error" });
  }
}

async function requireAdminAction(args: ActionFunctionArgs, action: ActionFunction) {
  if (isAdmin(authProvider.principal)) return action(args);
  throw new Error("user is not allowed to access this action");
}
