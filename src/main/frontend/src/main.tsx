import ReactDOM from "react-dom/client";
import { createBrowserRouter, redirect, RouterProvider } from "react-router-dom";
import "./index.css";
import Landing from "./views/landing.tsx";
import MainLayout from "./views/main-layout.tsx";
import { authProvider } from "./auth/auth.ts";
import RegistrationView, { action as registrationAction } from "./views/registration/registraiton-view.tsx";
import LoginView, { action as loginAction } from "./views/login/login-view.tsx";
import AuthenticationView, { loader as authLoader } from "./views/authenticate/authentication-view.tsx";
import AboutView from "./views/about/about-view.tsx";
import React from "react";
import PollView from "./views/poll/poll-view.tsx";

// TODO Add error fallback boundary
const router = createBrowserRouter([
  {
    path: "/",
    async loader() {
      await authProvider.fetchPrincipal();
      return { principal: authProvider.principal };
    },
    element: <MainLayout />,
    children: [
      {
        index: true,
        element: <Landing />,
      },
      {
        path: "about",
        handle: {},
        element: <AboutView />,
      },
      {
        path: "/login",
        element: <LoginView />,
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
        async loader() {
          if (await authProvider.isAuthenticated()) return redirect("/");
          return null;
        },
      },
      {
        path: "/register",
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
]);

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <RouterProvider router={router} />
  </React.StrictMode>,
);
