import { ActionFunctionArgs, json, LoaderFunctionArgs, redirect, useFetcher, useLoaderData, useParams } from "react-router-dom";
import ContextMenu from "../../components/navigation/context-menu.tsx";
import React from "react";
import { fetcher } from "../../utils/http.ts";
import { User } from "./users-layout.tsx";
import toast from "react-hot-toast";
import Avatar from "../../components/avatar.tsx";
import { MdDeleteForever, MdEmail } from "react-icons/md";
import ButtonIcon from "../../components/navigation/button-icon.tsx";
import Modal, { closeModal } from "../../components/modals/modal.tsx";
import { LiaUserEditSolid } from "react-icons/lia";

export async function loader({ params }: LoaderFunctionArgs) {
  const response = await fetcher({ path: "/api/v1/users/" + params.userId });

  if (!response.ok) {
    toast.error("Failed to fetch user");
    return redirect("/users");
  }

  return json((await response.json()) as User);
}

export async function deleteUser({ params }: ActionFunctionArgs) {
  const response = await fetcher({ path: "/api/v1/users/" + params.userId, method: "DELETE" });
  if (!response.ok) {
    toast.error("Failed to delete user");
    return json(null);
  }
  toast.success("Successfully deleted user");
  return redirect("/users");
}

export default function ShowUserView() {
  const user = useLoaderData() as User;

  return (
    <React.Fragment>
      <ContextMenu>
        <ButtonIcon tooltip="Edit user" icon={<LiaUserEditSolid />} />
        <DeleteAction />
      </ContextMenu>
      <div className="flex h-2/3 flex-col items-center justify-center gap-2">
        <div className="flew-wrap flex flex-row items-center gap-4">
          <Avatar user={user} className="base-100 w-32 rounded-full" />
          <JoinDate date={user.createdAt} />
        </div>
        <div className="flex flex-col gap-2">
          <h1 className={"pt-2 font-mono text-3xl uppercase"}>{user.name}</h1>
          <div className="flex flex-row flex-wrap gap-2">
            {user.roles.map((role) => (
              <div key={role} className="badge badge-accent badge-sm">
                {role}
              </div>
            ))}
          </div>
          <div className="flex flex-row items-center gap-2">
            <MdEmail className="text-xl" />
            <span>{user.email}</span>
          </div>
        </div>
      </div>
    </React.Fragment>
  );
}

function DeleteAction() {
  const fetcher = useFetcher();
  const { userId } = useParams();

  return (
    <Modal actionElement={<ButtonIcon icon={<MdDeleteForever className="text-2xl text-warning sm:text-3xl" />} tooltip="Delete user" />}>
      <h3 className="text-xl font-bold">Remove User</h3>
      <p className="py-4">Are you sure you want to delete this user?</p>
      <div className="modal-action">
        <fetcher.Form method="delete" action={`/users/${userId}/delete`} className="flex flex-row items-end gap-4">
          <button type="button" className="btn" onClick={closeModal}>
            Cancel
          </button>
          <button className="btn btn-warning" type="submit">
            Delete
          </button>
        </fetcher.Form>
      </div>
    </Modal>
  );
}

const JoinDate = ({ date }: { date: string }) => {
  const joinDate = new Date(date);
  const month = joinDate.toLocaleString("default", { month: "short" });
  const day = joinDate.getDate();
  const year = joinDate.getFullYear();

  return (
    <div className="inline-flex flex-col items-center rounded-lg border border-neutral bg-base-200 p-2">
      <div className="text-xs font-semibold uppercase">Joined</div>
      <div className="flex flex-row items-center gap-2">
        <span className="text-3xl font-bold">{day}</span>
        <div className="flex flex-col items-start">
          <span className="text-sm font-medium uppercase">{month}</span>
          <span className="text-xs">{year}</span>
        </div>
      </div>
    </div>
  );
};
