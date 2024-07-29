import { Link, useOutletContext } from "react-router-dom";
import { IoAddCircleSharp } from "react-icons/io5";
import { User } from "./users-layout.tsx";
import ContextMenu from "../../components/navigation/context-menu.tsx";
import IconLink from "../../components/navigation/icon-link.tsx";
import RequireAdmin from "../require-admin.tsx";
import Avatar from "../../components/avatar.tsx";

export default function UsersView() {
  const { users } = useOutletContext() as { users: User[] };

  return (
    <div className="flex h-full flex-col items-center gap-2">
      <ContextMenu>
        <RequireAdmin>
          <IconLink to={"/users/new"} tooltip="Add user" icon={<IoAddCircleSharp className="text-2xl text-accent sm:text-3xl" />} />
        </RequireAdmin>
      </ContextMenu>
      <div className="flex flex-row flex-wrap items-center justify-center gap-2 sm:gap-4">
        {users.map((user) => (
          <UserAvatar user={user} />
        ))}
      </div>
    </div>
  );
}

function UserAvatar({ user }: { user: User }) {
  return (
    <Link
      to={"/users/" + user.id}
      className="flex w-full max-w-xs flex-row items-center gap-4 rounded-lg border border-neutral bg-base-200 p-2 hover:bg-base-300"
    >
      <Avatar user={user} />
      <div>{user.name}</div>
    </Link>
  );
}

/*
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
*/
