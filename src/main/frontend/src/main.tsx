import React from "react";
import ReactDOM from "react-dom/client";
import {
  createHashRouter,
  Link,
  Navigate,
  RouterProvider,
  useNavigate,
  useSearchParams,
} from "react-router-dom";
import "./index.css";
import logo from "./assets/react.svg";

const router = createHashRouter([
  {
    path: "/",
    element: <Index />,
  },
  {
    path: "/about",
    element: <About />,
  },
  {
    path: "/login",
    element: <Login />,
  },
  {
    path: "/authenticate",
    element: <Authenticate />,
  },
]);

function Index() {
  return (
    <div className="flex h-full flex-col items-center gap-2 bg-cyan-200 p-4">
      <h1 className="text-3xl font-bold">Hello world!</h1>
      <img src={logo} alt="logo" width={600} />
      <Link to={"/about"}>About</Link>
      <Link to={"/login"}>Login</Link>
    </div>
  );
}

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
    <div className="flex h-full flex-col items-center justify-center">
      <div className="text-3xl">About</div>
      <div className="flex flex-col items-center justify-center gap-3">
        Message from server:{" "}
        {response.map((user) => (
          <pre key={user.id} className="w-full rounded-md bg-cyan-200 p-2">
            <UpdateRoles user={user} />
            {/*{JSON.stringify(user, null, 2)}*/}
          </pre>
        ))}
      </div>
      <Link className="hover:bg-cyan-400" to="/">
        Home
      </Link>
    </div>
  );
}

function Login() {
  const [submitted, setSubmitted] = React.useState(false);

  function onSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();

    const target = e.target as HTMLFormElement;
    const formData = new FormData(target);
    const email = formData.get("email") as string;

    fetch("/auth/web/login", {
      method: "POST",
      body: JSON.stringify({ email }),
      headers: {
        "Content-Type": "application/json",
      },
    })
      .then(() => setSubmitted(true))
      .catch((err) => console.error(err));
  }

  const form = (
    <form onSubmit={onSubmit} className="flex w-full max-w-md flex-col gap-2">
      <input
        className="w-full appearance-none rounded-md border border-gray-300 p-2"
        id="email"
        type="email"
        placeholder="Enter your email"
        name="email"
      />
      <button
        type="submit"
        className="rounded-md border border-gray-300 bg-cyan-200 p-2"
      >
        Submit
      </button>
    </form>
  );

  return (
    <div className="flex h-full flex-col items-center justify-center gap-3">
      {submitted ? (
        <div>
          <div className="text-lg">Submitted</div>
          <Link to="/">Back</Link>
        </div>
      ) : (
        form
      )}
    </div>
  );
}

function Authenticate() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [error, setError] = React.useState(false);

  const email = searchParams.get("email");
  const token = searchParams.get("token");

  React.useEffect(() => {
    if (!token || !email) return;

    fetch(
      `/auth/web/authenticate?${new URLSearchParams({ email, token }).toString()}`,
      { method: "POST" },
    )
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

  if (!token || !email) return <Navigate to={"/"} />;

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
