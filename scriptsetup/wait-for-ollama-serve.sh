echo "Wait for Ollama $1:$2 serve..."
until curl -s -o /dev/null -w "%{http_code}" "http://$1:$2" | grep -q "200"; do
 sleep 1
done
echo "Ollama has been served."
exit
