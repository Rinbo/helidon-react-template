import React from "react";
import ReactDOM from "react-dom/client";
import {
  createHashRouter,
  Link,
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
    fetch("/api/v1/users")
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
          <pre className="rounded-md bg-cyan-200 p-2">
            {JSON.stringify(user, null, 2)}
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

    fetch("/api/v1/login", {
      method: "POST",
      body: JSON.stringify({ email }),
      headers: {
        "Content-Type": "application/json",
      },
    })
      .then((_res) => setSubmitted(true))
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
  let [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [error, setError] = React.useState(false);

  const email = searchParams.get("email");
  const token = searchParams.get("token");
  console.log(email, token);

  React.useEffect(() => {
    const params = new URLSearchParams({ email, token });
    fetch(`/api/v1/authenticate?${params.toString()}`, { method: "POST" })
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
  }, []);

  //if (!token || !email) return <Navigate to={"/"} />;

  if (error) {
    return <div className="text-lg text-red-400">Authentication failed</div>;
  }

  return <div>Attempting authentication</div>;
}

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <RouterProvider router={router} />
  </React.StrictMode>,
);
