import { Link } from "react-router-dom";
import { ReactNode } from "react";

export default function IconLink({ icon, to, tooltip }: { icon: ReactNode; to: string; tooltip?: string }) {
  return (
    <Link to={to} className="btn btn-ghost btn-sm tooltip tooltip-bottom flex px-1" data-tip={tooltip}>
      {icon}
    </Link>
  );
}
