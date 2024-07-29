import { Link, useMatches } from "react-router-dom";

type Match = {
  id: string;
  data: object;
  pathname: string;
  handle?: { crumb: (data?: object) => string };
};

export default function Breadcrumb() {
  const matches = useMatches() as Match[];

  const augmentedMatches = matches
    .filter((match) => Boolean(match?.handle?.crumb))
    .map((match) => {
      return { ...match, crumb: match.handle?.crumb(match?.data) };
    });

  return (
    <div className="breadcrumbs text-xs sm:text-sm">
      <ul>
        {augmentedMatches.map((match) => (
          <li key={match.id}>
            <Link to={match.pathname}>{match.crumb}</Link>
          </li>
        ))}
      </ul>
    </div>
  );
}
