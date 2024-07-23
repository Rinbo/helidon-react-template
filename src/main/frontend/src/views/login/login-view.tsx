import LoginForm from "./login-form.tsx";
import { ActionFunctionArgs, json, redirect } from "react-router-dom";
import { fetcher } from "../../utils/http.ts";

export async function action({ request }: ActionFunctionArgs) {
  const response = await fetcher({ path: "/auth/web/login", method: "POST", body: await request.json() });
  if (response.ok) return redirect("/poll"); //TODO pass to waiter view
  return json({ error: "Unable to proceed with login" });
}

export default function LoginView() {
  return (
    <div className="flex h-full flex-col items-center justify-center gap-4 p-4">
      <LoginForm />
    </div>
  );
}
