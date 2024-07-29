import React from "react";
import { User } from "../views/users/users-layout.tsx";
import { Await, useAsyncValue } from "react-router-dom";
import { sha256 } from "../utils/misc-utils.ts";

export default function Avatar({ user, className }: { user: User; className?: string }) {
  return (
    <React.Suspense>
      <Await resolve={sha256(user.email)}>
        <div className="avatar">
          <div className={className ?? "base-100 w-12 rounded-full"}>
            <AsyncGravatarImage />
          </div>
        </div>
      </Await>
    </React.Suspense>
  );
}

function AsyncGravatarImage() {
  const hashedEmail = useAsyncValue();
  return <img src={`https://gravatar.com/avatar/${hashedEmail}?d=identicon&s=128`} alt="an avatar" />;
}
