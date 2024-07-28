import React from "react";
import ContextMenu from "../../components/navigation/context-menu.tsx";
import RegistrationForm from "../registration/registration-form.tsx";
import { ActionFunctionArgs, redirect } from "react-router-dom";
import { fetcher } from "../../utils/http.ts";

export async function action({ request }: ActionFunctionArgs) {
  await fetcher({ path: "/api/v1/users", method: "POST", body: await request.json() });
  return redirect("/users");
}

export default function NewUser() {
  return (
    <React.Fragment>
      <ContextMenu />
      <div className="flex h-full flex-col items-center justify-center">
        <div className="w-full max-w-lg">
          <RegistrationForm action="/users/new" />
        </div>
      </div>
    </React.Fragment>
  );
}
