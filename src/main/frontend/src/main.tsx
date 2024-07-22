import ReactDOM from "react-dom/client";
import { createBrowserRouter, LoaderFunction, LoaderFunctionArgs, redirect, RouterProvider } from "react-router-dom";
import "./index.css";
import Landing from "./views/landing.tsx";
import MainLayout from "./views/main-layout.tsx";
import { authProvider } from "./auth/auth.ts";
import RegistrationView, { action as registrationAction } from "./views/registration/registraiton-view.tsx";
import LoginView, { action as loginAction } from "./views/login/login-view.tsx";
import AuthenticationView, { loader as authLoader } from "./views/authenticate/authentication-view.tsx";
import AboutView from "./views/about/about-view.tsx";
import PollView from "./views/poll/poll-view.tsx";
import React from "react";

// TODO Add error fallback boundary
const router = createBrowserRouter([
  {
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
      },
      {
        path: "about",
        loader: (args) => requireAuth(args, nullLoader),
        handle: {},
        element: <AboutView />,
      },
      {
        path: "/login",
        element: <LoginView />,
        loader: redirectIfAuthenticated,
        action: loginAction,
      },
      {
        path: "/verify",
        loader: authLoader,
        element: <AuthenticationView />,
      },
      {
        path: "/poll",
        element: <PollView />,
        loader: redirectIfAuthenticated,
      },
      {
        path: "/register",
        element: <RegistrationView />,
        action: registrationAction,
      },
      { path: "/profile", element: <div>Profile</div> },
    ],
  },
  {
    path: "/logout",
    async action() {
      await authProvider.logout();
      return redirect("/");
    },
  },
]);

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <RouterProvider router={router} />
  </React.StrictMode>,
);

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
