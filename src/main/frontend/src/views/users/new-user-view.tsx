import React from "react";
import ContextMenu from "../../components/navigation/context-menu.tsx";
import RegistrationForm from "../registration/registration-form.tsx";
import { ActionFunctionArgs, json, redirect } from "react-router-dom";
import { fetcher } from "../../utils/http.ts";
import toast from "react-hot-toast";

export async function action({ request }: ActionFunctionArgs) {
  const response = await fetcher({ path: "/api/v1/users", method: "POST", body: await request.json() });

  if (!response.ok) {
    toast.error("Registration failed");
    return json(null);
  }

  toast.success("New user created");
  return redirect("/users");
}

export default function NewUserView() {
  return (
    <React.Fragment>
      <ContextMenu />
      <div className="flex h-full flex-col items-center justify-center">
        <div className="w-full max-w-lg p-4">
          <h1 className="mb-4 text-center text-xl uppercase sm:mb-6">Register a new user</h1>
          <RegistrationForm action="/users/new" />
        </div>
      </div>
    </React.Fragment>
  );
}
