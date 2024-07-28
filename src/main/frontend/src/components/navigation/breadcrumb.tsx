import { Link, useMatches } from "react-router-dom";

type Match = {
  id: string;
  pathname: string;
  handle?: Record<string, string>;
};

export default function Breadcrumb() {
  const matches = useMatches() as Match[];
  const crumbs = matches.filter((match) => Boolean(match?.handle?.label));

  return (
    <div className="breadcrumbs text-xs sm:text-sm">
      <ul>
        {crumbs.map((crumb) => (
          <li key={crumb.id}>
            <Link to={crumb.pathname}>{crumb.handle?.label}</Link>
          </li>
        ))}
      </ul>
    </div>
  );
}
