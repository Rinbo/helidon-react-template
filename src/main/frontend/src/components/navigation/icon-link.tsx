import { Link } from "react-router-dom";
import React, { ReactElement } from "react";

type Props = {
  icon: ReactElement;
  to: string;
  tooltip?: string;
};
export default function IconLink({ icon, to, tooltip }: Props) {
  return (
    <Link to={to} className="btn btn-ghost btn-sm tooltip tooltip-bottom flex px-1" data-tip={tooltip}>
      {React.cloneElement(icon, { className: icon.props.className ?? "text-2xl text-accent sm:text-3xl" })}
    </Link>
  );
}
