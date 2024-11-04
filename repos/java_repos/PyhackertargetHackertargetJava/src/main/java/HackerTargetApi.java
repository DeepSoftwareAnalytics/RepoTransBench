import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class HackerTargetApi {
    
    private static final String[] requestUrls = {
        "https://api.hackertarget.com/mtr/?q=",
        "https://api.hackertarget.com/nping/?q=",
        "https://api.hackertarget.com/dnslookup/?q=",
        "https://api.hackertarget.com/reversedns/?q=",
        "https://api.hackertarget.com/hostsearch/?q=",
        "https://api.hackertarget.com/findshareddns/?q=",
        "https://api.hackertarget.com/zonetransfer/?q=",
        "https://api.hackertarget.com/whois/?q=",
        "https://api.hackertarget.com/geoip/?q=",
        "https://api.hackertarget.com/reverseiplookup/?q=",
        "https://api.hackertarget.com/nmap/?q=",
        "https://api.hackertarget.com/subnetcalc/?q=",
        "https://api.hackertarget.com/httpheaders/?q=",
        "https://api.hackertarget.com/pagelinks/?q="
    };

    public String hackertargetApi(int choice, String target) {
        String requestUrl = requestUrls[choice - 1];
        String url = requestUrl + target;

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                HttpEntity entity = response.getEntity();
                return EntityUtils.toString(entity);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        String logo = "  _               _              _                          _\n" +
                " | |_   __ _  __ | |__ ___  _ _ | |_  __ _  _ _  __ _  ___ | |_\n" +
                " | ' \\ / _` |/ _|| / // -_)| '_||  _|/ _` || '_|/ _` |/ -_)|  _|\n" +
                " |_||_|\\__,_|\\__||_\\_\\\\___||_|   \\__|\\__,_||_|  \\__, |\\___| \\__|\n" +
                "                                                |___/\n" +
                "  Ismail Tasdelen\n" +
                " | github.com/ismailtasdelen | linkedin.com/in/ismailtasdelen |\n";

        String menu = "[1] Traceroute\n" +
                      "[2] Ping Test\n" +
                      "[3] DNS Lookup\n" +
                      "[4] Reverse DNS\n" +
                      "[5] Find DNS Host\n" +
                      "[6] Find Shared DNS\n" +
                      "[7] Zone Transfer\n" +
                      "[8] Whois Lookup\n" +
                      "[9] IP Location Lookup\n" +
                      "[10] Reverse IP Lookup\n" +
                      "[11] TCP Port Scan\n" +
                      "[12] Subnet Lookup\n" +
                      "[13] HTTP Header Check\n" +
                      "[14] Extract Page Links\n" +
                      "[15] Version\n" +
                      "[16] Exit\n";

        System.out.println(logo);
        System.out.println(menu);
        
        // Just a sort of console UI
        java.util.Scanner scanner = new java.util.Scanner(System.in);
        while (true) {
            System.out.print("Which option number: ");
            String choice = scanner.next();

            if ("16".equals(choice)) {
                System.out.println("Exiting...");
                break;
            }

            System.out.print("[+] Target: ");
            String target = scanner.next();
            System.out.println();

            String result;
            HackerTargetApi api = new HackerTargetApi();

            switch (choice) {
                case "1":
                    System.out.println("[+] Traceroute script running..");
                    result = api.hackertargetApi(1, target);
                    break;
                case "2":
                    System.out.println("[+] Ping Test script running..");
                    result = api.hackertargetApi(2, target);
                    break;
                case "3":
                    System.out.println("[+] DNS Lookup script running..");
                    result = api.hackertargetApi(3, target);
                    break;
                case "4":
                    System.out.println("[+] Reverse DNS script running..");
                    result = api.hackertargetApi(4, target);
                    break;
                case "5":
                    System.out.println("[+] Find DNS Host script running..");
                    result = api.hackertargetApi(5, target);
                    break;
                case "6":
                    System.out.println("[+] Find Shared DNS script running..");
                    result = api.hackertargetApi(6, target);
                    break;
                case "7":
                    System.out.println("[+] Zone Transfer script running..");
                    result = api.hackertargetApi(7, target);
                    break;
                case "8":
                    System.out.println("[+] Whois Lookup script running..");
                    result = api.hackertargetApi(8, target);
                    break;
                case "9":
                    System.out.println("[+] IP Location Lookup script running..");
                    result = api.hackertargetApi(9, target);
                    break;
                case "10":
                    System.out.println("[+] Reverse IP Lookup script running..");
                    result = api.hackertargetApi(10, target);
                    break;
                case "11":
                    System.out.println("[+] TCP Port Scan script running..");
                    result = api.hackertargetApi(11, target);
                    break;
                case "12":
                    System.out.println("[+] Subnet Lookup script running..");
                    result = api.hackertargetApi(12, target);
                    break;
                case "13":
                    System.out.println("[+] HTTP Header Check script running..");
                    result = api.hackertargetApi(13, target);
                    break;
                case "14":
                    System.out.println("[+] Extract Page Links script running..");
                    result = api.hackertargetApi(14, target);
                    break;
                case "15":
                    System.out.println("[+] Version Checking..");
                    try {
                        Thread.sleep(2000);
                        System.out.println("[+] Version: 2.0");
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                default:
                    System.out.println("Invalid Option!\n");
                    continue;
            }

            System.out.println(result);
            System.out.println(menu);
        }
        scanner.close();
    }
}
