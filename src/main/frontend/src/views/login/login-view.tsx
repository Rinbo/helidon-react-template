import { ActionFunctionArgs, json, useSearchParams } from "react-router-dom";
import { fetcher } from "../../utils/http.ts";
import LoginWrapper from "./login-wrapper.tsx";

export async function action({ request }: ActionFunctionArgs) {
  const body = await request.json();
  const response = await fetcher({ path: "/auth/web/login", method: "POST", body: body });
  if (response.ok) return json({ email: body.email });
  return json({ error: "Unable to proceed with login. Try again later." });
}

export type LoginAction = { email?: string; error?: string };

export default function LoginView() {
  const [searchParams] = useSearchParams();

  return (
    <div className="flex h-full flex-col items-center justify-center gap-4 p-4">
      <LoginWrapper passedEmail={searchParams.get("email") ?? undefined} />
    </div>
  );
}
