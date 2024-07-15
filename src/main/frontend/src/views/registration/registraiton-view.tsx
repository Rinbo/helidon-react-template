import { ActionFunctionArgs, json, redirect } from "react-router-dom";
import RegistrationForm from "./registration-form.tsx";
import { fetcher } from "../../utils/http.ts";

export async function action({ request }: ActionFunctionArgs) {
  const response = await fetcher({ path: "/auth/web/register", method: "POST", body: await request.json() });

  if (!response.ok) return json({ error: "Registration failed" });

  return redirect("/");
}

export default function RegistrationView() {
  return (
    <div className="flex h-full flex-col items-center justify-center gap-4 p-4">
      <div>REGISTRATION VIEW</div>
      <RegistrationForm />
    </div>
  );
}
