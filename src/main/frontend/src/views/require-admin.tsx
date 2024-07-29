import { useAuth } from "./main-layout.tsx";
import { PropsWithChildren } from "react";

export default function RequireAdmin(props: PropsWithChildren) {
  const { principal } = useAuth();

  if (!principal || !principal.roles.includes("ADMIN")) return null;
  return props.children;
}
