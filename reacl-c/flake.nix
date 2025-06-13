{
  description = "Minimal devShell with Clojure";

  inputs.nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";

  outputs = { self, nixpkgs }: {
    devShells."aarch64-darwin".default = let
      system = "aarch64-darwin";
      pkgs = import nixpkgs { inherit system; };
    in pkgs.mkShell {
      packages = [
        pkgs.clojure
        # python3 -m http.server 4000
        pkgs.python3
      ];
    };
  };
}
