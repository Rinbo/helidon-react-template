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
          <UserAvatar key={user.id} user={user} />
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
