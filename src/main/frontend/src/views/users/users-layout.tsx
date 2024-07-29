import { json, Outlet, useLoaderData } from "react-router-dom";
import { fetcher } from "../../utils/http.ts";

export const ROLE_LIST = ["USER", "ADMIN", "WEBMASTER"] as const;
export type Role = (typeof ROLE_LIST)[number];
export type User = { id: number; email: string; name: string; roles: Role[]; createdAt: string; updatedAt: string };

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
