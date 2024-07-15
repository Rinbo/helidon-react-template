import { Outlet, useLoaderData } from "react-router-dom";
import { Principal } from "../auth/auth.ts";

export default function MainLayout() {
  const { principal } = useLoaderData() as { principal: Principal | null };

  // TODO based on handle data, I should be able to redirect before outlet is rendered right?
  return (
    <main className="h-full">
      <Outlet context={principal} />
    </main>
  );
}
