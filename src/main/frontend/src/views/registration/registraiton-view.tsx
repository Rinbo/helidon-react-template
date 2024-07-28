import { ActionFunctionArgs, json, Link, redirect } from "react-router-dom";
import RegistrationForm from "./registration-form.tsx";
import { fetcher } from "../../utils/http.ts";
import { MdOutlineJoinInner } from "react-icons/md";

export async function action({ request }: ActionFunctionArgs) {
  const formData = await request.json();
  const response = await fetcher({ path: "/auth/web/register", method: "POST", body: formData });

  if (!response.ok) return json({ error: "Registration failed" });

  return redirect("/login?" + new URLSearchParams({ email: formData.email }));
}

export default function RegistrationView() {
  return (
    <div className="flex h-full flex-col items-center justify-center gap-4 p-2">
      <div className="flex w-full max-w-md flex-col items-center justify-center gap-2 rounded-lg border border-accent p-4">
        <MdOutlineJoinInner size={55} className="text-secondary" />
        <h1 className="mb-3 justify-center text-lg uppercase">Sign up</h1>
        <RegistrationForm action="/register" />
        <div className="mt-2 text-center">
          Already have an account?
          <br />
          Please{" "}
          <span>
            <Link className="link-info" to="/login">
              login here
            </Link>
          </span>
        </div>
      </div>
    </div>
  );
}
