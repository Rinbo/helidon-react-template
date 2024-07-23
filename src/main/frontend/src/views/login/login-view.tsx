import { ActionFunctionArgs, json } from "react-router-dom";
import { fetcher } from "../../utils/http.ts";
import LoginWrapper from "./login-wrapper.tsx";

export async function action({ request }: ActionFunctionArgs) {
  const response = await fetcher({ path: "/auth/web/login", method: "POST", body: await request.json() });
  if (response.ok) return json({ success: true });
  return json({ error: "Unable to proceed with login. Try again later." });
}

export type LoginAction = { success?: boolean; error?: string };

export default function LoginView() {
  return (
    <div className="flex h-full flex-col items-center justify-center gap-4 p-4">
      <LoginWrapper />
    </div>
  );
}
