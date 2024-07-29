import React from "react";
import { User } from "../views/users/users-layout.tsx";
import { getGravatarUrl } from "../utils/misc-utils.ts";

export default function Avatar({ user, className }: { user: User; className?: string }) {
  const [url, setUrl] = React.useState<string>("");

  React.useEffect(() => {
    getGravatarUrl(user.email).then(setUrl);
  }, []);

  return (
    <div className="avatar">
      <div className={className ?? "base-100 w-12 rounded-full"}>
        <img src={url ?? null} alt="an avatar" />
      </div>
    </div>
  );
}
