import { Outlet, useLoaderData } from "react-router-dom";
import { Principal } from "../auth/auth.ts";

export default function MainLayout() {
  const { principal } = useLoaderData() as { principal: Principal | null };

  console.log(principal, "FROM MAIN LAYOUT");
  return (
    <main className="h-full">
      <Outlet />
    </main>
  );
}
