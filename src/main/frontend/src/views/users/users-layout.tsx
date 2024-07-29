import { json, Outlet, useLoaderData } from "react-router-dom";
import { fetcher } from "../../utils/http.ts";

export type User = { id: number; email: string; name: string; roles: string[]; createdAt: string; updatedAt: string };

export async function loader() {
  const response = await fetcher({ path: "/api/v1/users" });
  return json({ users: await response.json() });
}

export default function UsersLayout() {
  const { users } = useLoaderData() as { users: User[] };

  return (
    <section className={"h-full"}>
      <Outlet context={{ users }} />
    </section>
  );
}
