def jobsMapping = [
  tags: [jobName:"App GWAPortal", jobTags: "reload", extraVars: "app_generic_image_tag: latest"]
]

buildDockerImage([
    imageName: "gwaportal-backend",
    pushRegistryNamespace: "nordborglab/gwaportal",
    pushBranches: ['master', 'fix_mail'],
    tower: jobsMapping
])