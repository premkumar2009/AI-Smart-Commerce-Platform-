param(
  [string]$BaseUrl = 'http://localhost:8082'
)

$products = @()
$page = 0
while ($true) {
  $response = Invoke-RestMethod "$BaseUrl/api/products?page=$page&size=50&sort=createdAt&direction=asc"
  $products += @($response.content)
  if ($page + 1 -ge $response.totalPages) { break }
  $page++
}

$duplicateImages = @($products | Group-Object imageUrl | Where-Object { $_.Count -gt 1 })
$queries = @(
  'wireless headphones under 5000',
  'Sony headphones under 10000',
  'gaming keyboard under 5000',
  'black backpack under 3000'
)

Write-Output "Total products: $($products.Count)"
Write-Output "Categories: $(@($products | Group-Object categoryName).Count)"
Write-Output "Products without image URL: $(@($products | Where-Object { [string]::IsNullOrWhiteSpace($_.imageUrl) }).Count)"
Write-Output "Products with zero stock: $(@($products | Where-Object { $_.stock -le 0 }).Count)"
Write-Output "Duplicate image URL groups: $($duplicateImages.Count)"
foreach ($group in $duplicateImages) { Write-Output "  $($group.Count)x $($group.Name)" }
foreach ($query in $queries) {
  $result = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/ai/search" -ContentType 'application/json' -Body (@{ query = $query } | ConvertTo-Json)
  Write-Output "AI '$query': $(@($result.products).Count) products"
}

Write-Output 'Exact model/image matching is a manual verification step; this audit only checks presence, reachability, and duplicate URLs.'
