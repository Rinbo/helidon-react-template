import React from "react";
import { Await, json, useAsyncValue, useLoaderData } from "react-router-dom";
import { fetcher } from "../../utils/http.ts";
import { sha256 } from "../../utils/misc-utils.ts";

type User = { id: number; email: string; name: string; roles: string[] };

export async function loader() {
  const response = await fetcher({ path: "/api/v1/users" });
  return json({ users: await response.json() });
}

export default function UsersView() {
  const { users } = useLoaderData() as { users: User[] };

  return (
    <div className="flex h-full flex-col items-center gap-6">
      <div className="text-2xl uppercase">Users</div>
      <div className="flex flex-row flex-wrap items-center justify-center gap-6">
        {users.map((user) => (
          <React.Suspense key={user.id} fallback={<p>"loading user"</p>}>
            <Await resolve={sha256(user.email)}>
              <UserAvatar user={user} />
            </Await>
          </React.Suspense>
        ))}
      </div>
    </div>
  );
}

function UserAvatar({ user }: { user: User }) {
  const hashedEmail = useAsyncValue();

  return (
    <div className="flex w-full max-w-xs flex-row items-center gap-4 rounded-lg border border-neutral bg-base-200 p-2">
      <div className="avatar">
        <div className="base-100 w-12 rounded-full">
          <img src={`https://gravatar.com/avatar/${hashedEmail}?d=identicon`} alt="an avatar" />
        </div>
      </div>
      <div>{user.name}</div>
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
