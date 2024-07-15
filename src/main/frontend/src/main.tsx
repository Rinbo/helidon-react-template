import React from "react";
import ReactDOM from "react-dom/client";
import { createBrowserRouter, Link, redirect, RouterProvider, useNavigate, useSearchParams } from "react-router-dom";
import "./index.css";
import Landing from "./views/landing.tsx";
import MainLayout from "./views/main-layout.tsx";
import { authProvider } from "./auth/auth.ts";
import RegistrationView, { action as registrationAction } from "./views/registration/registraiton-view.tsx";
import LogoutForm from "./views/logout/logout-form.tsx";
import LoginView, { action as loginAction } from "./views/login/login-view.tsx";

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
        element: <About />,
      },
      {
        path: "/login",
        element: <LoginView />,
        action: loginAction,
      },
      {
        path: "/verify",
        element: <Authenticate />,
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

type User = { id: number; email: string; name: string; roles: string[] };
type ResponseType = User[];

function About() {
  const [response, setResponse] = React.useState<ResponseType>([]);

  React.useEffect(() => {
    fetch("/api/v1/users", { headers: { withCredentials: "true" } })
      .then((res) => res.json())
      .then((data) => setResponse(data))
      .catch((e) => console.error(e));
  }, []);

  return (
    <div className="flex h-full flex-col items-center justify-center gap-2">
      <LogoutForm />
      <div className="text-3xl">About</div>
      <div>Message from server:</div>
      <div className="flex flex-row flex-wrap items-center justify-center gap-3">
        {response.map((user) => (
          <pre key={user.id} className="rounded-md bg-cyan-200 p-2">
            <UpdateRoles user={user} />
          </pre>
        ))}
      </div>
      <Link className="mt-10 hover:bg-cyan-400" to="/">
        Home
      </Link>
    </div>
  );
}

function Authenticate() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [error, setError] = React.useState(false);

  console.log("I AM IN AUTHENTICATE");

  const email = searchParams.get("email");
  const token = searchParams.get("token");

  React.useEffect(() => {
    if (!token || !email) return;

    fetch(`/auth/web/authenticate?${new URLSearchParams({ email, token }).toString()}`, { method: "POST" })
      .then((res) => {
        if (res.ok) {
          console.log("Authenticated successfully");
          navigate("/");
        } else {
          console.error("Authentication failed");
          setError(true);
        }
      })
      .catch((err) => {
        console.error("Authentication failed", err);
        setError(true);
      });
  }, [email, navigate, token]);

  //if (!token || !email) return <Navigate to={"/"} />;

  if (error) {
    return <div className="text-lg text-red-400">Authentication failed</div>;
  }

  return <div>Attempting authentication</div>;
}

const roleList = ["USER", "ADMIN", "WEBMASTER"] as const;

function UpdateRoles({ user }: { user: User }) {
  function onSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const formData = new FormData(e.target as HTMLFormElement);

    const selectedRoles = formData.getAll("role-select") as string[];
    console.log(selectedRoles);

    fetch(`/api/v1/users/${user.id}/roles`, {
      method: "PUT",
      body: JSON.stringify(selectedRoles),
      headers: { "Content-Type": "application/json" },
    }).then((res) => {
      if (res.ok) {
        console.log("SUCCESS");
      } else {
        console.error("ERROR");
      }
    });
  }

  const roles = user.roles;

  return (
    <div>
      <div>UserId: {user.id}</div>
      <div>Name: {user.name}</div>
      <form onSubmit={onSubmit}>
        <select name={"role-select"} multiple defaultValue={roles}>
          {roleList.map((role) => (
            <option key={role} value={role}>
              {role}
            </option>
          ))}
        </select>
        <button type="submit" className="m-2 p-4">
          Save
        </button>
      </form>
    </div>
  );
}

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <RouterProvider router={router} />
  </React.StrictMode>,
);
