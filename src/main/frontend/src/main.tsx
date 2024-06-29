import React from "react";
import ReactDOM from "react-dom/client";
import { createHashRouter, Link, RouterProvider } from "react-router-dom";
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
]);

function Index() {
  return (
    <div className="bg-red-700 p-4">
      <h1 className="text-3xl font-bold underline">Hello world!</h1>
      <img src={logo} alt="logo" />
      <Link to={"/about"}>About</Link>
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

  console.log(response, "RESPONSE");

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

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <RouterProvider router={router} />
  </React.StrictMode>,
);
