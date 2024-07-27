import { ActionFunctionArgs, json, redirect } from "react-router-dom";
import RegistrationForm from "./registration-form.tsx";
import { fetcher } from "../../utils/http.ts";

export async function action({ request }: ActionFunctionArgs) {
  const formData = await request.json();
  const response = await fetcher({ path: "/auth/web/register", method: "POST", body: formData });

  if (!response.ok) return json({ error: "Registration failed" });

  return redirect("/login?" + new URLSearchParams({ email: formData.email }));
}

export default function RegistrationView() {
  return (
    <div className="flex h-full flex-col items-center justify-center gap-4 p-2">
      <RegistrationForm />
    </div>
  );
}
