import React from "react";
import LogoutForm from "../logout/logout-form.tsx";
import { Link } from "react-router-dom";

type User = { id: number; email: string; name: string; roles: string[] };
type ResponseType = User[];

export default function UsersView() {
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

const roleList = ["USER", "ADMIN", "WEBMASTER"] as const;

function UpdateRoles({ user }: { user: User }) {
  function onSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const formData = new FormData(e.target as HTMLFormElement);

    const selectedRoles = formData.getAll("role-select") as string[];

    fetch(`/api/v1/users/${user.id}/roles`, {
      method: "PUT",
      body: JSON.stringify(selectedRoles),
      headers: { "Content-Type": "application/json" },
    }).then((res) => {
      if (res.ok) {
        console.info("SUCCESS");
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
