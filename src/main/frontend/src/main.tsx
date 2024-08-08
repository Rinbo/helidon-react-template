import ReactDOM from "react-dom/client";
import { createBrowserRouter, RouterProvider } from "react-router-dom";
import "./index.css";
import React from "react";
import { routes } from "./routes.tsx";
import { Toaster } from "react-hot-toast";

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <Toaster position="bottom-right" />
    <RouterProvider router={createBrowserRouter(routes, { basename: "/ui" })} />
  </React.StrictMode>,
);
